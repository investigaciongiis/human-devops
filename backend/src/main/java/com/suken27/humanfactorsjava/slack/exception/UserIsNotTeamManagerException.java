package com.suken27.humanfactorsjava.slack.exception;

public class UserIsNotTeamManagerException extends RuntimeException {
    
    public UserIsNotTeamManagerException(String slackId) {
        super("The user with slack id " + slackId + " is not a team manager, but it was required.");
    }

}
