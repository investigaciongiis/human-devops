package com.suken27.humanfactorsjava.rest.api;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.suken27.humanfactorsjava.model.controller.ModelController;
import com.suken27.humanfactorsjava.model.dto.TeamManagerDto;
import com.suken27.humanfactorsjava.model.exception.IncorrectLoginException;

@RestController
public class UserController {
    
    @Autowired
    private ModelController modelController;

    @PutMapping("/user/password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> json) {
        String teamManagerEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        String newPassword = json.get("newPassword");
        String oldPassword = json.get("oldPassword");
        if(newPassword == null || newPassword.isEmpty() || oldPassword == null || oldPassword.isEmpty()) {
            return ResponseEntity.badRequest().body("Empty password.");
        }
        try {
            return ResponseEntity.ok().body(modelController.updateTeamManagerPassword(teamManagerEmail, oldPassword, newPassword));
        } catch(IncorrectLoginException e) {
            return ResponseEntity.badRequest().body("Incorrect password.");
        } catch(Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user/integration")
    public ResponseEntity<?> integrationCompleted() {
        String teamManagerEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        TeamManagerDto teamManager = modelController.getTeamManager(teamManagerEmail);
        return ResponseEntity.ok().body(teamManager.getSlackId() != null);
    }

}
