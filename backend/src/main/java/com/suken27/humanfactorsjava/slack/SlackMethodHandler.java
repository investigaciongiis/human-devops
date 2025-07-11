package com.suken27.humanfactorsjava.slack;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.users.UsersListResponse;
import com.slack.api.model.User;
import com.suken27.humanfactorsjava.model.controller.ModelController;
import com.suken27.humanfactorsjava.model.dto.QuestionDto;
import com.suken27.humanfactorsjava.model.dto.TeamDto;
import com.suken27.humanfactorsjava.model.dto.TeamMemberDto;
import com.suken27.humanfactorsjava.model.dto.UserDto;
import com.suken27.humanfactorsjava.model.exception.MemberAlreadyInTeamException;
import com.suken27.humanfactorsjava.model.exception.TeamManagerNotFoundException;
import com.suken27.humanfactorsjava.rest.exception.MemberInAnotherTeamException;
import com.suken27.humanfactorsjava.slack.exception.UserNotFoundInWorkspaceException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SlackMethodHandler {

    @Autowired
    private ModelController modelController;

    private final Slack slack = Slack.getInstance();


    private final ConcurrentMap<String, User> usersStoreById    = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, User> usersStoreByEmail = new ConcurrentHashMap<>();


    private static final int MAX_RETRIES = 3;
    private static final Duration CACHE_TTL = Duration.ofMinutes(1);
    private volatile Instant lastSync = Instant.EPOCH;


    public String getUserId(String email, String botToken)
            throws UserNotFoundInWorkspaceException, SlackApiException, IOException {
        return getUserByEmail(email, botToken).getId();
    }

    public String getUserEmail(String id, String botToken)
            throws UserNotFoundInWorkspaceException, SlackApiException, IOException {
        return getUserById(id, botToken).getProfile().getEmail();
    }

    public User getUserById(String id, String botToken)
            throws SlackApiException, IOException, UserNotFoundInWorkspaceException {
        User user = usersStoreById.get(id);
        if (user == null) {
            fetchUsers(botToken);
            user = usersStoreById.get(id);
        }
        if (user == null) {
            throw new UserNotFoundInWorkspaceException(id);
        }
        return user;
    }

    public User getUserByEmail(String email, String botToken)
            throws SlackApiException, IOException, UserNotFoundInWorkspaceException {
        User user = usersStoreByEmail.get(email);
        if (user == null) {
            fetchUsers(botToken);
            user = usersStoreByEmail.get(email);
        }
        if (user == null) {
            throw new UserNotFoundInWorkspaceException(email);
        }
        return user;
    }

    public TeamDto checkTeamManager(String id, String slackBotToken)
            throws UserNotFoundInWorkspaceException, SlackApiException, IOException, TeamManagerNotFoundException,
                   ClassNotFoundException, NoSuchMethodException, SchedulerException {

        String email = getUserEmail(id, slackBotToken);
        TeamDto team = modelController.getTeam(email);
        if (team == null) {
            throw new TeamManagerNotFoundException(email);
        }
        for (TeamMemberDto member : team.getMembers()) {
            if (member.getSlackId() == null) {
                try{
                    member.setSlackId(getUserId(member.getEmail(), slackBotToken));
                } catch(UserNotFoundInWorkspaceException e){
                    System.out.println("UserNotFoundInWorkspaceException - mail: " + member.getEmail());
                }
            }
        }
        team.setSlackBotToken(slackBotToken);
        modelController.updateTeamManagerSlackId(email, id);
        return modelController.updateTeam(team);
    }

    public TeamDto addTeamMember(String teamManagerId, String userId, String slackBotToken)
            throws UserNotFoundInWorkspaceException, SlackApiException, IOException, TeamManagerNotFoundException,
                   MemberAlreadyInTeamException, MemberInAnotherTeamException {
        return modelController.addTeamMember(getUserEmail(teamManagerId, slackBotToken),
                                             getUserEmail(userId, slackBotToken), userId);
    }

    public Map<UserDto, List<QuestionDto>> launchQuestions(String teamManagerId, String slackBotToken)
            throws UserNotFoundInWorkspaceException, SlackApiException, IOException, TeamManagerNotFoundException {
        return modelController.launchQuestions(getUserEmail(teamManagerId, slackBotToken));
    }

    public String answerQuestion(String userId, Long questionId, String answer, String slackBotToken)
            throws NumberFormatException, UserNotFoundInWorkspaceException, SlackApiException, IOException {
        return modelController.answerQuestion(getUserEmail(userId, slackBotToken),
                                              questionId, Double.parseDouble(answer));
    }

    private synchronized void fetchUsers(String botToken)
            throws SlackApiException, IOException {

        if (Duration.between(lastSync, Instant.now()).compareTo(CACHE_TTL) < 0) {
            return;
        }

        MethodsClient client = slack.methods();
        int attempt = 0;

        while (attempt < MAX_RETRIES) {
            try {
                UsersListResponse resp = client.usersList(r -> r.token(botToken));

                usersStoreById.clear();
                usersStoreByEmail.clear();
                resp.getMembers().stream()
                    .filter(u -> !u.isDeleted() && !u.isBot() && u.isEmailConfirmed())
                    .forEach(u -> {
                        usersStoreById.put(u.getId(), u);
                        if (u.getProfile() != null && u.getProfile().getEmail() != null) {
                            usersStoreByEmail.put(u.getProfile().getEmail(), u);
                        }
                    });

                lastSync = Instant.now();
                return;

            } catch (SlackApiException e) {
                if ("ratelimited".equals(e.getError())) {
                    int retryAfter = Integer.parseInt(e.getResponse().header("Retry-After"));
                    attempt++;
                    try {
                        Thread.sleep((retryAfter + 1L) * 1000L);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Interrupted while waiting for rate-limit", ie);
                    }
                } else {
                    throw e;
                }
            }
        }

        throw new SlackApiException(null, "users.list failed after " + MAX_RETRIES + " retries");
    }
}
