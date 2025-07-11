package com.suken27.humanfactorsjava.slack.exception;

public class UserNotFoundInWorkspaceException extends RuntimeException {
    
    public UserNotFoundInWorkspaceException(String id) {
        super("User with id/email '" + id + "'' not found among the list of all users in the workspace.");
    }

}
