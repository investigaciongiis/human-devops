package com.suken27.humanfactorsjava.rest.api;

import java.util.stream.Collectors;

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.suken27.humanfactorsjava.model.controller.ModelController;
import com.suken27.humanfactorsjava.model.exception.IncorrectLoginException;
import com.suken27.humanfactorsjava.rest.dto.AuthDto;
import com.suken27.humanfactorsjava.rest.dto.JwtResponseDto;
import com.suken27.humanfactorsjava.rest.exception.IncorrectEmailFormatException;
import com.suken27.humanfactorsjava.rest.exception.IncorrectPasswordFormatException;
import com.suken27.humanfactorsjava.rest.util.ApiValidator;
import com.suken27.humanfactorsjava.security.JwtUtils;

import lombok.extern.log4j.Log4j2;

@RestController
@Log4j2
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private ApiValidator validator;

    @Autowired
    private ModelController modelController;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Validated @RequestBody AuthDto authDto) {
        log.debug("Authentication attempt. Data received: " + authDto.toString());
        if (authDto == null || authDto.getEmail() == null || authDto.getPassword() == null) {
            return ResponseEntity.badRequest().body("Empty email or password.");
        }
        try {
            modelController.checkUser(authDto.getEmail(), authDto.getPassword());
        } catch (IncorrectLoginException e) {
            return ResponseEntity.notFound().build();
        }
        // At this point the data should be correct and the user should exist
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(authDto.getEmail(), authDto.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String role = userDetails.getAuthorities().stream().map(item -> item.getAuthority())
                .collect(Collectors.toList()).get(0);

        return ResponseEntity.ok(new JwtResponseDto(jwt, userDetails.getUsername(), role));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Validated @RequestBody AuthDto authDto) throws SchedulerException {
        log.debug("Registration attempt. Data received: " + authDto.toString());
        if (!validator.isValidEmail(authDto.getEmail())) {
            return ResponseEntity.badRequest().body(new IncorrectEmailFormatException(authDto.getEmail()));
        }
        if (!validator.isValidPassword(authDto.getPassword())) {
            return ResponseEntity.badRequest().body(new IncorrectPasswordFormatException());
        }
        modelController.registerTeamManager(authDto.getEmail(), authDto.getPassword());
        return ResponseEntity.ok("User registered successfully.");
    }

}
