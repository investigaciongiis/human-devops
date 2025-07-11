package com.suken27.humanfactorsjava.rest.exception;

public class IncorrectEmailFormatException extends RuntimeException {
    
    public IncorrectEmailFormatException(String email) {
        super("The email '" + email + "' format is not valid.");
    }

}
