package com.suken27.humanfactorsjava.repository;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.suken27.humanfactorsjava.model.HumanFactorFactory;
import com.suken27.humanfactorsjava.model.Team;
import com.suken27.humanfactorsjava.model.TeamManager;

import jakarta.transaction.Transactional;

@SpringBootTest
public class TeamRepositoryTest {

    private final static String TEST_TEAM_MANAGER_EMAIL = "testTeamManager@test.test";
    private final static String TEST_TEAM_MANAGER_PASSWORD = "testPassword";

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    TeamManagerRepository teamManagerRepository;

    private static TeamManager teamManager;

    @BeforeAll
    private static void createTestTeamManager(@Autowired TeamManagerRepository teamManagerRepository, @Autowired HumanFactorFactory humanFactorFactory) {
        teamManager = new TeamManager(humanFactorFactory);
        teamManager.setEmail(TEST_TEAM_MANAGER_EMAIL);
        teamManager.setPassword(TEST_TEAM_MANAGER_PASSWORD);
        teamManager = teamManagerRepository.save(teamManager);
    }

    @AfterAll
    private static void deleteTestTeamManager(@Autowired TeamManagerRepository teamManagerRepository) {
        teamManagerRepository.delete(teamManager);
    }

    @Test
    @Transactional
    void testFindByTeamManagerEmail() {
        assertNotNull(teamManager);
        assertNotNull(teamManager.getTeam());
        assertNotNull(teamManager.getTeam().getManager()); 
        assertEquals(teamManager, teamManager.getTeam().getManager());
        Team team = teamRepository.findByTeamManagerEmail(teamManager.getEmail());
        assertNotNull(team);
        assertNotNull(team.getManager());
        assertNotNull(team.getManager().getEmail());
        assertEquals(TEST_TEAM_MANAGER_EMAIL, team.getManager().getEmail());
    }

}
