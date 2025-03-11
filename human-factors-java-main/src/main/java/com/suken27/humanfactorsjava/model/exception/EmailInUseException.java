package com.suken27.humanfactorsjava.model.exception;

public class EmailInUseException extends RuntimeException {
    
    public EmailInUseException(String email) {
        super("The email '" + email + "' is already registered in the database.");
    }

}
