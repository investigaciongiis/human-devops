package com.suken27.humanfactorsjava.rest.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.suken27.humanfactorsjava.model.Team;
import com.suken27.humanfactorsjava.model.TeamManager;
import com.suken27.humanfactorsjava.repository.TeamManagerRepository;
import com.suken27.humanfactorsjava.repository.TeamRepository;
import com.suken27.humanfactorsjava.rest.dto.AuthDto;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private AuthController controller;

    @Autowired
    private TeamManagerRepository teamManagerRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    private static AuthDto dto;

    @BeforeAll
    private static void setAuthDto() {
        dto = new AuthDto("testCreateManager@test.test", "passwordTest");
    }

    @Test
    void contextLoads() {
        assertNotNull(controller);
    }

    @Test
    @Transactional
    void testRegisterUser() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.post("/signup")
                .content("{\"email\":\"" + dto.getEmail() + "\",\"password\":\"" + dto.getPassword() + "\"}")
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
        TeamManager entity = teamManagerRepository.findByEmail(dto.getEmail());
        assertNotNull(entity);
        assertEquals(dto.getEmail(), entity.getEmail());
        assertTrue(passwordEncoder.matches(dto.getPassword(), entity.getPassword()));
        assertNotNull(entity.getTeam());

        Team team = teamRepository.findByTeamManagerEmail(dto.getEmail());
        assertNotNull(team);
        assertNotNull(team.getManager());
        assertEquals(entity, team.getManager());
    }

    @Test
    @Transactional
    void testErrorRegisterUser() throws Exception {
        
        String invalidEmail1 = "unformattedText";
        String invalidEmail2 = "almostEmail@somethingcom";
        String invalidEmail3 = "almostEmailsomething.com";

        this.mockMvc.perform(MockMvcRequestBuilders.post("/signup")
                .content("{\"email\":\"" + dto.getEmail() + "\",\"password\":\"\"}")
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
        TeamManager entity = teamManagerRepository.findByEmail(dto.getEmail());
        assertNull(entity);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/signup")
                .content("{\"email\":\"\",\"password\":\"\"}")
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
        entity = teamManagerRepository.findByEmail(dto.getEmail());
        assertNull(entity);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/signup")
                .content("{\"email\":\"" + invalidEmail1 + "\",\"password\":" + dto.getPassword() + "\"\"}")
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest())
                .andExpect(content().string(".*format is not valid.*"));
        entity = teamManagerRepository.findByEmail(dto.getEmail());
        assertNull(entity);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/signup")
                .content("{\"email\":\"" + invalidEmail2 + "\",\"password\":" + dto.getPassword() + "\"\"}")
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
        entity = teamManagerRepository.findByEmail(dto.getEmail());
        assertNull(entity);
        
        this.mockMvc.perform(MockMvcRequestBuilders.post("/signup")
                .content("{\"email\":\"" + invalidEmail3 + "\",\"password\":" + dto.getPassword() + "\"\"}")
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
        entity = teamManagerRepository.findByEmail(dto.getEmail());
        assertNull(entity);
    }

    @Test
    @Transactional
    void testAuthenticateUser() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.post("/login")
                .content("{\"email\":\"" + dto.getEmail() + "\",\"password\":" + dto.getPassword() + "\"\"}")
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound());
    }

}
