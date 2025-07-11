package com.suken27.humanfactorsjava.model.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.suken27.humanfactorsjava.model.HumanFactorType;
import com.suken27.humanfactorsjava.model.TeamHumanFactor;
import com.suken27.humanfactorsjava.model.TeamManager;
import com.suken27.humanfactorsjava.model.dto.ActionDto;
import com.suken27.humanfactorsjava.model.dto.TeamManagerDto;
import com.suken27.humanfactorsjava.repository.TeamManagerRepository;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Slf4j
public class ModelControllerTest {

    private static final String TEAM_MANAGER_EMAIL = "teamManager@test.test";
    private static final String TEAM_MANAGER_PASSWORD = "teamManagerPassword";

    @Test
    @Transactional
    void testAnswerQuestion(@Autowired ModelController modelController, @Autowired TeamManagerRepository teamManagerRepository) throws SchedulerException {
        TeamManagerDto teamManagerDto = modelController.registerTeamManager(TEAM_MANAGER_EMAIL, TEAM_MANAGER_PASSWORD);
        assertNotNull(teamManagerDto);
        assertEquals(TEAM_MANAGER_EMAIL, teamManagerDto.getEmail());
        Optional<TeamManager> optional = teamManagerRepository.findById(teamManagerDto.getId());
        assertTrue(optional.isPresent());
        TeamManager teamManager = optional.get();
        assertEquals(TEAM_MANAGER_EMAIL, teamManager.getEmail());
        String answerText = modelController.answerQuestion(teamManager.getEmail(), teamManager.getHumanFactors().get(0).getQuestions().get(0).getId(), 0.0);
		assertNotNull(answerText);
    }

    @Test
    @Transactional
    void testGetRecommendedActions(@Autowired ModelController modelController, @Autowired TeamManagerRepository teamManagerRepository) throws SchedulerException {
        TeamManagerDto teamManagerDto = modelController.registerTeamManager(TEAM_MANAGER_EMAIL, TEAM_MANAGER_PASSWORD);
        assertNotNull(teamManagerDto);
        assertEquals(TEAM_MANAGER_EMAIL, teamManagerDto.getEmail());
        // This code artificially sets the human factor scores to random values
        Optional<TeamManager> optional = teamManagerRepository.findById(teamManagerDto.getId());
        assertTrue(optional.isPresent());
        TeamManager entity = optional.get();
        Map<HumanFactorType, TeamHumanFactor> humanFactors = entity.getTeam().getHumanFactors();
        for(TeamHumanFactor humanFactor : humanFactors.values()) {
            humanFactor.setScore(Math.random());
        }
        teamManagerRepository.save(entity);
        List<ActionDto> recommendedActions = modelController.getRecommendedActions(teamManagerDto.getEmail());
        assertNotNull(recommendedActions);
        assertFalse(recommendedActions.isEmpty());
        for(ActionDto actionDto : recommendedActions) {
            log.info(actionDto.toString());
        }
    }
}
