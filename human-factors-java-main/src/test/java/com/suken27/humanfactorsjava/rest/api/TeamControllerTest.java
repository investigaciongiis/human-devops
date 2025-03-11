package com.suken27.humanfactorsjava.rest.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.suken27.humanfactorsjava.model.HumanFactorFactory;
import com.suken27.humanfactorsjava.model.Team;
import com.suken27.humanfactorsjava.model.TeamManager;
import com.suken27.humanfactorsjava.model.TeamMember;
import com.suken27.humanfactorsjava.repository.TeamManagerRepository;
import com.suken27.humanfactorsjava.repository.TeamMemberRepository;
import com.suken27.humanfactorsjava.repository.TeamRepository;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
public class TeamControllerTest {

    private final static String TEST_TEAM_MANAGER_EMAIL = "testTeamManager@test.test";
    private final static String TEST_TEAM_MANAGER_PASSWORD = "testPassword";
    private final static String TEST_TEAM_MEMBER_EMAIL = "testTeamMember@test.test";
    private final static String TEST_QUESTION_SENDING_TIME = "23:33";

    @Autowired
    TeamController controller;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    TeamManagerRepository teamManagerRepository;

    @Autowired
    TeamMemberRepository teamMemberRepository;

    @Autowired
    HumanFactorFactory humanFactorFactory;

    @Autowired
    MockMvc mockMvc;

    private static TeamManager teamManager;

    @BeforeAll
    private static void createTestTeamManager(@Autowired TeamManagerRepository teamManagerRepository, @Autowired HumanFactorFactory humanFactorFactory) {
        teamManager = new TeamManager(humanFactorFactory);
        teamManager.setEmail(TEST_TEAM_MANAGER_EMAIL);
        teamManager.setPassword(TEST_TEAM_MANAGER_PASSWORD);
        teamManagerRepository.save(teamManager);
    }

    @Test
    @WithMockUser(username=TEST_TEAM_MANAGER_EMAIL, roles={"USER"})
    @Transactional
    public void testAddTeamMember() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/teams").content(TEST_TEAM_MEMBER_EMAIL)).andExpect(status().isOk());
        Team team = teamRepository.findByTeamManagerEmail(TEST_TEAM_MANAGER_EMAIL);
        assertNotNull(team);
        assertNotNull(team.getMembers());
        assertEquals(1, team.getMembers().size());
        assertEquals(TEST_TEAM_MEMBER_EMAIL, team.getMembers().get(0).getEmail());
    }

    @Test
    @WithMockUser(username=TEST_TEAM_MANAGER_EMAIL, roles={"USER"})
    @Transactional
    public void testRemoveTeamMember() throws Exception {
        TeamMember teamMember = new TeamMember(humanFactorFactory);
        teamMember.setEmail(TEST_TEAM_MEMBER_EMAIL);
        teamManager.getTeam().addMember(teamMember);
        teamRepository.save(teamManager.getTeam());
        mockMvc.perform(MockMvcRequestBuilders.delete("/teams/" + TEST_TEAM_MEMBER_EMAIL)).andExpect(status().isOk());
        Team team = teamRepository.findByTeamManagerEmail(TEST_TEAM_MANAGER_EMAIL);
        assertNotNull(team);
        assertNotNull(team.getMembers());
        assertTrue(team.getMembers().isEmpty());
        assertNull(teamMemberRepository.findByEmail(TEST_TEAM_MEMBER_EMAIL));
        List<TeamMember> results = teamMemberRepository.findByEmailIncludingDeleted(TEST_TEAM_MEMBER_EMAIL);
        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    @WithMockUser(username=TEST_TEAM_MANAGER_EMAIL, roles={"USER"})
    @Transactional
    void testModifyQuestionSendingTime() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/teams/time").content(TEST_QUESTION_SENDING_TIME)).andExpect(status().isOk());
        Team team = teamRepository.findByTeamManagerEmail(TEST_TEAM_MANAGER_EMAIL);
        assertNotNull(team);
        LocalTime time = team.getZonedQuestionSendingTime();
        assertNotNull(time);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        assertEquals(TEST_QUESTION_SENDING_TIME, time.format(dateTimeFormatter));
    }

}
