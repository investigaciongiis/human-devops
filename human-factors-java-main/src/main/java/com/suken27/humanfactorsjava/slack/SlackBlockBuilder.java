package com.suken27.humanfactorsjava.slack;

import static com.slack.api.model.block.Blocks.*;
import static com.slack.api.model.block.composition.BlockCompositions.*;
import static com.slack.api.model.block.element.BlockElements.*;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.element.BlockElement;
import com.suken27.humanfactorsjava.model.dto.QuestionDto;
import com.suken27.humanfactorsjava.model.dto.TeamDto;
import com.suken27.humanfactorsjava.model.dto.TeamMemberDto;

@Component
public class SlackBlockBuilder {

    private static final int MAX_BLOCKS_PER_MESSAGE = 50;

    public List<List<LayoutBlock>> questionBlocks(List<QuestionDto> questions) {
        List<List<LayoutBlock>> blocks = new ArrayList<>();
        List<LayoutBlock> messageBlocks = new ArrayList<>();
        for (QuestionDto question : questions) {
            List<LayoutBlock> questionBlocks = questionBlock(question);
            // Checks if adding one more question would exceed the maximum amount of blocks
            // per message (including the divider)
            if (messageBlocks.size() + questionBlocks.size() + 1 > MAX_BLOCKS_PER_MESSAGE) {
                blocks.add(messageBlocks);
                messageBlocks = new ArrayList<>();
            }
            messageBlocks.addAll(questionBlocks);
            messageBlocks.add(divider());
        }
        blocks.add(messageBlocks);
        return blocks;
    }

    public List<LayoutBlock> questionBlock(QuestionDto question) {
        List<LayoutBlock> blocks = new ArrayList<>();
        blocks.add(section(section -> section
                .text(markdownText(mt -> mt.text(question.getQuestionText())))));
        List<BlockElement> elements = new ArrayList<>();
        // iterate over the entries of the map sorted by key value
        List<Double> optionKeys = question.getOptions().keySet().stream().sorted().toList();
        for (Double key : optionKeys) {
            elements.add(button(b -> b
                    .text(plainText(question.getOptions().get(key)))
                    .value(key.toString())
                    .actionId("question_answer_action_" + question.getId() + "_" + key.toString())));
        }
        blocks.add(actions(action -> action
                .elements(elements)));
        return blocks;
    }

    public List<LayoutBlock> listTeamMembers(List<LayoutBlock> blocks, TeamDto team) {
        for (TeamMemberDto member : team.getMembers()) {
            if (member.getSlackId() != null) {
                blocks.add(section(section -> section.text(markdownText(mt -> mt.text(
                        "<@" + member.getSlackId() + "> is a team member.")))));
            }
        }
        return blocks;
    }

    public List<LayoutBlock> addTeamMemberBlock(List<LayoutBlock> blocks, String blockId, String selectActionId,
            String buttonActionId) {
        blocks.add(actions(action -> action
                .blockId(blockId)
                .elements(asElements(
                        usersSelect(us -> us
                                .actionId(selectActionId)
                                .placeholder(plainText(
                                        "Pick a user from the dropdown list"))),
                        button(b -> b
                                .actionId(buttonActionId)
                                .text(plainText("Add selection")))))));
        return blocks;
    }

}
