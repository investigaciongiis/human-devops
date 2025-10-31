package com.suken27.humanfactorsjava.model.dto;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.time.DayOfWeek;

import com.suken27.humanfactorsjava.model.Team;
import com.suken27.humanfactorsjava.model.TeamMember;

import lombok.Data;

@Data
public class TeamDto {
    
    private Long id;
    private Long manager;
    private List<TeamMemberDto> members;
    private String questionSendingTime;
    private int questionsPerDay;
    private String slackBotToken;
    private String questionFrequency;     // "DAILY" | "WEEKLY" | "MONTHLY"
    private String questionDayOfWeek;     // "MON".."SUN"
    private Integer questionDayOfMonth;   // 1..28

    public TeamDto() {
        super();
    }

    public TeamDto(Team team) {
        id = team.getId();
        manager = team.getManager().getId();
        members = new ArrayList<>();
        for (TeamMember member : team.getMembers()) {
            members.add(new TeamMemberDto(member));
        }
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        questionSendingTime = team.getZonedQuestionSendingTime().format(dateTimeFormatter);
        questionsPerDay = team.getQuestionsPerDay();
        slackBotToken = team.getSlackBotToken();
        questionFrequency = team.getQuestionFrequency() == null ? null : team.getQuestionFrequency().name();
        questionDayOfWeek = team.getQuestionDayOfWeek() == null ? null : team.getQuestionDayOfWeek().name();
        questionDayOfMonth = team.getQuestionDayOfMonth();
    }
}
