package com.suken27.humanfactorsjava.model.exception;

public class TeamMemberNotFoundException extends RuntimeException {
    
    public TeamMemberNotFoundException(String email) {
        super("The email '" + email + "' does not belong to a registered team member.");
    }

}
