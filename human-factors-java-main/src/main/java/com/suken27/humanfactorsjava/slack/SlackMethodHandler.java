package com.suken27.humanfactorsjava.slack;

import java.io.IOException;
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

@Component
public class SlackMethodHandler {

    @Autowired
    private ModelController modelController;

    // These are the users that are currently in the workspace in Slack
    private final ConcurrentMap<String, User> usersStoreById = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, User> usersStoreByEmail = new ConcurrentHashMap<>();

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
            throws UserNotFoundInWorkspaceException, SlackApiException, IOException, TeamManagerNotFoundException, ClassNotFoundException, NoSuchMethodException, SchedulerException {
        String email = getUserEmail(id, slackBotToken);
        TeamDto team = modelController.getTeam(email);
        if (team == null) {
            throw new TeamManagerNotFoundException(email);
        }
        // TODO: Decouple this logic from the model
        for (TeamMemberDto member : team.getMembers()) {
            if (member.getSlackId() == null) {
                member.setSlackId(getUserId(member.getEmail(), slackBotToken));
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

    public String answerQuestion(String userId, Long questionId, String answer, String slackBotToken) throws NumberFormatException, UserNotFoundInWorkspaceException, SlackApiException, IOException {
        return modelController.answerQuestion(getUserEmail(userId, slackBotToken), questionId, Double.parseDouble(answer));
    }

    private void fetchUsers(String botToken) throws SlackApiException, IOException {
        MethodsClient client = Slack.getInstance().methods();
        // Call the users.list method using the built-in WebClient
        UsersListResponse result = client.usersList(r -> r
                .token(botToken));
        for (User user : result.getMembers()) {
            if (!user.isDeleted() && !user.isBot() && user.isEmailConfirmed()) {
                usersStoreById.put(user.getId(), user);
                usersStoreByEmail.put(user.getProfile().getEmail(), user);
            }
        }
    }

}
