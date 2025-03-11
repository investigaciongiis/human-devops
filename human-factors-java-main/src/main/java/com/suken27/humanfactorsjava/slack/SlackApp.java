package com.suken27.humanfactorsjava.slack;

import static com.slack.api.model.block.Blocks.*;
import static com.slack.api.model.block.composition.BlockCompositions.*;
import static com.slack.api.model.view.Views.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.slack.api.app_backend.interactive_components.payload.BlockActionPayload.Action;
import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import com.slack.api.bolt.context.Context;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.block.ActionsBlock;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.event.AppHomeOpenedEvent;
import com.slack.api.model.view.View;
import com.slack.api.model.view.ViewState.Value;
import com.suken27.humanfactorsjava.model.dto.QuestionDto;
import com.suken27.humanfactorsjava.model.dto.TeamDto;
import com.suken27.humanfactorsjava.model.dto.UserDto;
import com.suken27.humanfactorsjava.model.exception.MemberAlreadyInTeamException;
import com.suken27.humanfactorsjava.model.exception.TeamManagerNotFoundException;
import com.suken27.humanfactorsjava.rest.exception.MemberInAnotherTeamException;
import com.suken27.humanfactorsjava.slack.exception.UserIsNotTeamManagerException;
import com.suken27.humanfactorsjava.slack.exception.UserNotFoundInWorkspaceException;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class SlackApp {

        // TODO: Refactor this class to move whatever can be moved to SlackBlockBuilder

        // TODO: Maybe almost every action could be resolved using response?

        @Autowired
        private SlackMethodHandler slackMethodHandler;

        @Autowired
        private SlackBlockBuilder slackBlockBuilder;

        private static final String USER_SELECT_ACTION_ID = "team_member_select_action";
        private static final String ADD_MEMBER_BUTTON_ACTION_ID = "team_member_add_action";
        private static final String USER_SELECT_BLOCK_ID = "team_member_add_block";

        @Bean
        public AppConfig loadOAuthConfig(Environment environment) {
                return AppConfig.builder()
                                .singleTeamBotToken(null)
                                .signingSecret(environment.getProperty("com.suken27.humanfactors.slack.signingSecret"))
                                .clientId(environment.getProperty("com.suken27.humanfactors.slack.clientID"))
                                .clientSecret(environment.getProperty("com.suken27.humanfactors.slack.clientSecret"))
                                .scope(environment.getProperty("com.suken27.humanfactors.slack.scope"))
                                .userScope(environment.getProperty("com.suken27.humanfactors.slack.userScope"))
                                .oauthInstallPath(environment.getProperty("com.suken27.humanfactors.slack.installPath"))
                                .oauthRedirectUriPath(environment
                                                .getProperty("com.suken27.humanfactors.slack.redirectURIPath"))
                                .build();
        }

        @Bean
        public App initSlackApp(AppConfig config) {
                App app = new App(config).asOAuthApp(true);
                app.event(AppHomeOpenedEvent.class, (payload, ctx) -> {
                        AppHomeOpenedEvent event = payload.getEvent();
                        List<LayoutBlock> blocks = addTeamBlocks(event.getUser(), ctx.getBotToken());
                        // Build a Home tab view
                        View appHomeView = view(view -> view
                                        .type("home")
                                        .blocks(blocks));
                        // Update the App Home for the given user
                        if (event.getView() == null) {
                                updateView(appHomeView, event.getUser(), null, ctx);
                        } else {
                                updateView(appHomeView, event.getUser(), null, ctx);
                        }
                        return ctx.ack();
                });
                app.command("/questions", (req, ctx) -> {
                        try {
                                launchQuestions(ctx);
                        } catch (Exception e) {
                                log.error("Error ocurred when using the SlackApi", e);
                        }
                        return ctx.ack("Questions launched");
                });
                return addActionHandlers(app);
        }

        private List<LayoutBlock> addTeamBlocks(String user, String botToken) {
                TeamDto team = null;
                List<LayoutBlock> blocks = new ArrayList<>();
                try {
                        team = slackMethodHandler.checkTeamManager(user, botToken);
                } catch (TeamManagerNotFoundException e) {
                        log.debug("Slack user with id {} tried to access team without being a TeamManager", user);
                        blocks.add(section(section -> section.text(markdownText(mt -> mt.text(
                                        "*You are not a team manager* :skull_and_crossbones:")))));
                } catch (UserNotFoundInWorkspaceException e) {
                        // This is really improbable, but we should handle it anyway
                        log.error("Slack user with id [{}] not found in workspace. This exception should never be thrown.",
                                        user, e);
                        blocks.add(section(section -> section.text(markdownText(mt -> mt.text(
                                        "*Unexpected error: Couldn't find the current user in the workspace* :warning:")))));
                } catch (SlackApiException e) {
                        if (e.getError() != null && e.getError().getError().equals("ratelimited")) {
                                log.info("Too many requests to the Slack Api, the maximum amount of requests has been exceeded",
                                                e);
                                blocks.add(section(section -> section.text(markdownText(mt -> mt.text(
                                                "*Too many requests: Please try again in "
                                                                + e.getResponse().header("Retry-After")
                                                                + " seconds * :warning:")))));
                        }
                        log.error("Error ocurred when using the SlackApi", e);
                        blocks.add(section(section -> section.text(markdownText(mt -> mt.text(
                                        "*Unexpected error: Error ocurred when using the SlackApi* :warning:")))));
                } catch (Exception e) {
                        log.error("Error ocurred when using the SlackApi", e);
                        blocks.add(section(section -> section.text(markdownText(mt -> mt.text(
                                        "*Unexpected error: Error ocurred when using the SlackApi* :warning:")))));
                }
                if (team == null) {
                        blocks.add(header(h -> h.text(plainText("You are not a team manager"))));
                        return blocks;
                }
                blocks.add(header(h -> h.text(plainText("You are a team manager"))));
                blocks.add(divider());
                slackBlockBuilder.listTeamMembers(blocks, team);
                blocks.add(divider());
                slackBlockBuilder.addTeamMemberBlock(blocks, USER_SELECT_BLOCK_ID, USER_SELECT_ACTION_ID,
                                ADD_MEMBER_BUTTON_ACTION_ID);
                return blocks;
        }

        private App addUserSelectionHandler(App app) {
                return app.blockAction(USER_SELECT_ACTION_ID, (req, ctx) -> {
                        log.debug("Team member select action received. Payload: {}", req.getPayload());
                        return ctx.ack();
                });
        }

        private App addActionHandlers(App app) {
                app = addUserSelectionHandler(app);
                app = addTeamMemberHandler(app);
                return addQuestionAnswerHandler(app);
        }

        private App addTeamMemberHandler(App app) {
                return app.blockAction(ADD_MEMBER_BUTTON_ACTION_ID, (req, ctx) -> {
                        log.debug("Team member add action received. Payload: {}", req.getPayload());
                        Map<String, Value> values = req.getPayload().getView().getState().getValues()
                                        .get(USER_SELECT_BLOCK_ID);
                        if (values == null) {
                                log.error("Team member add action received but no values were found in the actions block. Payload: {}",
                                                req.getPayload());
                                return ctx.ack();
                        }
                        Value value = values.get(USER_SELECT_ACTION_ID);
                        if (value == null) {
                                log.error("Team member add action received but the user select block was not among the values included. Payload: {}",
                                                req.getPayload());
                                return ctx.ack();
                        }
                        String selectedUserId = value.getSelectedUser();
                        if (selectedUserId == null) {
                                log.error("Team member add action received but no user was selected. Payload: {}",
                                                req.getPayload());
                                return ctx.ack();
                        }
                        String teamManagerId = req.getPayload().getUser().getId();
                        TeamDto team = null;
                        try {
                                team = slackMethodHandler.addTeamMember(teamManagerId, selectedUserId,
                                                ctx.getBotToken());
                                value.setSelectedUser(null);
                                View appHomeView = view(view -> view
                                                .type("home")
                                                .blocks(addTeamBlocks(teamManagerId, ctx.getBotToken())));
                                updateView(appHomeView, teamManagerId, null, ctx);
                                log.debug("Team member [{}] added to the team managed by [{}]", selectedUserId,
                                                teamManagerId);
                        } catch (MemberAlreadyInTeamException e) {
                                log.debug("Slack user with id [{}] tried to add a team member that is already in the team",
                                                teamManagerId);
                        } catch (MemberInAnotherTeamException e) {
                                log.debug("Slack user with id [{}] tried to add a team member that is already in another team",
                                                teamManagerId);
                        } catch (SlackApiException | IOException | UserNotFoundInWorkspaceException
                                        | TeamManagerNotFoundException e) {
                                log.error("Error ocurred when trying to add the team member [{}] to the team managed by [{}]",
                                                selectedUserId, teamManagerId, e);
                        }
                        return ctx.ack();
                });
        }

        private App addQuestionAnswerHandler(App app) {
                return app.blockAction(Pattern.compile("question_answer_action_.*"), (req, ctx) -> {
                        log.debug("Question answer action received. Payload: {}", req.getPayload());
                        Action action = req.getPayload().getActions().get(0);
                        String[] actionIdParts = action.getActionId().split("_");
                        String questionId = actionIdParts[3];
                        String answer = actionIdParts[4];
                        String answerText = slackMethodHandler.answerQuestion(ctx.getRequestUserId(), Long.parseLong(questionId), answer, ctx.getBotToken());
                        log.debug("Question [{}] answered with [{}]", questionId, answerText);
                        List<LayoutBlock> blocks = req.getPayload().getMessage().getBlocks();
                        ListIterator<LayoutBlock> iterator = blocks.listIterator();
                        boolean found = false;
                        while (iterator.hasNext() && !found) {
                                LayoutBlock block = iterator.next();
                                if (block instanceof ActionsBlock && block.getBlockId().equals(action.getBlockId())) {
                                        iterator.remove();
                                        iterator.add(section(section -> section.text(markdownText(mt -> mt.text(
                                                        "You answered: " + answerText)))));
                                        found = true;
                                }
                        }
                        ctx.respond(response -> response.blocks(blocks).replaceOriginal(true));
                        return ctx.ack();
                });
        }

        private void updateView(View view, String userId, String hash, Context context)
                        throws IOException, SlackApiException {
                context.client().viewsPublish(r -> {
                        r.view(view);
                        r.userId(userId);
                        r.hash(hash);
                        return r;
                });
        }

        private void launchQuestions(Context context) throws IOException, SlackApiException, UserNotFoundInWorkspaceException, ClassNotFoundException, NoSuchMethodException, SchedulerException {
                String userSlackId = context.getRequestUserId();
                try {
                        slackMethodHandler.checkTeamManager(userSlackId, context.getBotToken());
                } catch (TeamManagerNotFoundException e) {
                        throw new UserIsNotTeamManagerException(userSlackId);
                }
                Map<UserDto, List<QuestionDto>> questions = slackMethodHandler
                                .launchQuestions(context.getRequestUserId(), context.getBotToken());
                for (Entry<UserDto, List<QuestionDto>> entry : questions.entrySet()) {
                        log.debug("Sending questions to user [{}]", entry.getKey().getSlackId());
                        for (List<LayoutBlock> blocks : slackBlockBuilder.questionBlocks(entry.getValue())) {
                                context.asyncClient().chatPostMessage(r -> {
                                        r.channel(entry.getKey().getSlackId());
                                        r.blocks(blocks);
                                        r.token(context.getBotToken());
                                        r.text("Human factors daily questions");
                                        return r;
                                });
                        }
                }
        }

}