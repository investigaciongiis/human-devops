package com.suken27.humanfactorsjava.rest.exception;

public class MemberInAnotherTeamException extends RuntimeException {
    
    public MemberInAnotherTeamException(String email) {
        super("The user with email '" + email + "' is already registered in another team. A user cannot be registered in two teams.");
    }

}
