package com.suken27.humanfactorsjava.slack;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.slack.api.bolt.App;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.block.LayoutBlock;
import com.suken27.humanfactorsjava.model.dto.QuestionDto;
import com.suken27.humanfactorsjava.model.dto.UserDto;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class SlackController {

    @Autowired
    private App app;

    @Autowired
    private SlackBlockBuilder slackBlockBuilder;

    public void sendQuestionsToSlack(Map<UserDto, List<QuestionDto>> questionMap, String slackBotToken)
            throws SlackApiException, IOException {
        

        for (Map.Entry<UserDto, List<QuestionDto>> entry : questionMap.entrySet()) {
            log.debug("Sending questions to user [{}]", entry.getKey().getSlackId());
            for (List<LayoutBlock> blocks : slackBlockBuilder.questionBlocks(entry.getValue())) {
                app.client().chatPostMessage(r -> {
                    r.channel(entry.getKey().getSlackId());
                    r.blocks(blocks);
                    r.token(slackBotToken);
                    r.text("Human factors daily questions");
                    return r;
                });
            }
        }
    }

}
