package com.suken27.humanfactorsjava.rest.exception;

public class UserNotInTeamException extends RuntimeException {
    
    public UserNotInTeamException(String email) {
        super("User with email '" + email + "' is not a member of the team and cannot be removed from it.");
    }

}
