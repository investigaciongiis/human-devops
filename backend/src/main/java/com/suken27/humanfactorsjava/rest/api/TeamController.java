package com.suken27.humanfactorsjava.rest.api;

import java.io.IOException;
import java.time.LocalTime;
import java.util.List;
import java.time.DayOfWeek;

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.slack.api.methods.SlackApiException;
import com.suken27.humanfactorsjava.model.controller.ModelController;
import com.suken27.humanfactorsjava.model.dto.ActionDto;
import com.suken27.humanfactorsjava.model.dto.TeamDto;
import com.suken27.humanfactorsjava.model.dto.QuestionScheduleDto;

import com.suken27.humanfactorsjava.model.QuestionFrequency;
import com.suken27.humanfactorsjava.rest.exception.IncorrectEmailFormatException;
import com.suken27.humanfactorsjava.rest.exception.IncorrectTimeFormatException;
import com.suken27.humanfactorsjava.rest.util.ApiValidator;
import com.suken27.humanfactorsjava.slack.SlackMethodHandler;
import com.suken27.humanfactorsjava.slack.exception.UserNotFoundInWorkspaceException;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class TeamController {

    @Autowired
    private ModelController modelController;

    @Autowired
    private SlackMethodHandler slackMethodHandler;

    @Autowired
    private ApiValidator validator;

    @GetMapping("/teams")
    public ResponseEntity<?> getTeam() {
        String teamManagerEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        TeamDto team = modelController.getTeam(teamManagerEmail);
        
        if (team.getQuestionFrequency() == null) {
            team.setQuestionFrequency(QuestionFrequency.DAILY.name());
        }
        if (team.getQuestionDayOfWeek() == null) {
            team.setQuestionDayOfWeek(DayOfWeek.MONDAY.name());
        }
        if (team.getQuestionDayOfMonth() == 0) {
            team.setQuestionDayOfMonth(1);
        }
        return ResponseEntity.ok().body(team);
    }

    @PostMapping("/teams")
    public ResponseEntity<?> addTeamMember(@RequestBody String email) throws UserNotFoundInWorkspaceException, SlackApiException, IOException {
        if(email == null || !validator.isValidEmail(email)) {
            return ResponseEntity.badRequest().body(new IncorrectEmailFormatException(email));
        }
        String teamManagerEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        String id = null;
        try {
            id = slackMethodHandler.getUserId(email, teamManagerEmail);
        } catch(Exception e) {
            log.debug("Tried to retrieve member slack id for member [{}], but failed. No slack id will be added to the member.", email, e);
        }
        TeamDto team = modelController.addTeamMember(teamManagerEmail, email, id);
        return ResponseEntity.ok().body(team);
    }

    @DeleteMapping("/teams/{email}")
    public ResponseEntity<?> removeTeamMember(@PathVariable String email) {
        if(email == null || !validator.isValidEmail(email)) {
            return ResponseEntity.badRequest().body(new IncorrectEmailFormatException(email));
        }
        String teamManagerEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        TeamDto team = modelController.removeTeamMember(teamManagerEmail, email);
        return ResponseEntity.ok().body(team);
    }

    @PutMapping("/teams/time")
    public ResponseEntity<?> modifyQuestionSchedule(@RequestBody QuestionScheduleDto dto) throws SchedulerException {
        String teamManagerEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        LocalTime localTime = validator.parseTimeString(dto.getQuestionSendingTime());
        if (localTime == null) {
            return ResponseEntity.badRequest().body(new IncorrectTimeFormatException(dto.getQuestionSendingTime()));
        }

        QuestionFrequency frequency;
        try {
            frequency = dto.getQuestionFrequency() != null
                    ? QuestionFrequency.valueOf(dto.getQuestionFrequency())
                    : QuestionFrequency.DAILY;
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body("Invalid questionFrequency value.");
        }

        DayOfWeek dayOfWeek = null;
        if (dto.getQuestionDayOfWeek() != null) {
            try {
                dayOfWeek = DayOfWeek.valueOf(dto.getQuestionDayOfWeek());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("Invalid questionDayOfWeek value.");
            }
        }

        Integer dayOfMonth = (dto.getQuestionDayOfMonth() != null && dto.getQuestionDayOfMonth() >= 1 && dto.getQuestionDayOfMonth() <= 28)
                ? dto.getQuestionDayOfMonth()
                : 1;

        TeamDto team = modelController.modifyQuestionSchedule(
                teamManagerEmail, localTime, frequency, dayOfWeek, dayOfMonth);

        return ResponseEntity.ok().body(team);
    }

    @GetMapping("/teams/actions")
    public ResponseEntity<?> getActions() {
        String teamManagerEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        List<ActionDto> actions = modelController.getRecommendedActions(teamManagerEmail);
        return ResponseEntity.ok().body(actions);
    }

}
