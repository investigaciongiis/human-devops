package com.suken27.humanfactorsjava.model.scheduling;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.slack.api.bolt.App;
import com.slack.api.model.block.LayoutBlock;
import com.suken27.humanfactorsjava.model.controller.ModelController;
import com.suken27.humanfactorsjava.model.dto.QuestionDto;
import com.suken27.humanfactorsjava.model.dto.TeamDto;
import com.suken27.humanfactorsjava.model.dto.UserDto;
import com.suken27.humanfactorsjava.slack.SlackBlockBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QuestionSendingJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        ModelController modelController = (ModelController) context.getJobDetail().getJobDataMap()
                .get("modelController");
        App slackApp = (App) context.getJobDetail().getJobDataMap().get("slackApp");
        SlackBlockBuilder slackBlockBuilder = (SlackBlockBuilder) context.getJobDetail().getJobDataMap()
                .get("slackBlockBuilder");
        TeamDto team = (TeamDto) context.getJobDetail().getJobDataMap().get("team");
        String teamManagerEmail = (String) context.getJobDetail().getJobDataMap().get("teamManagerEmail");
        Map<UserDto, List<QuestionDto>> questions = modelController.launchQuestions(teamManagerEmail);
        for (Entry<UserDto, List<QuestionDto>> entry : questions.entrySet()) {
            log.debug("Sending questions to user [{}]", entry.getKey().getSlackId());
            for (List<LayoutBlock> blocks : slackBlockBuilder.questionBlocks(entry.getValue())) {
                try {
                    slackApp.client().chatPostMessage(r -> {
                        r.channel(entry.getKey().getSlackId());
                        r.blocks(blocks);
                        r.token(team.getSlackBotToken());
                        r.text("Human factors daily questions");
                        return r;
                    });
                } catch (Exception e) {
                    log.error("Error sending questions to user [{}]", entry.getKey().getSlackId(), e);
                }
            }
        }
    }

}
