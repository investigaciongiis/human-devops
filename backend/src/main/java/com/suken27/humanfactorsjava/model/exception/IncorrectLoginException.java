package com.suken27.humanfactorsjava.model.exception;

public class IncorrectLoginException extends RuntimeException {
    
    public IncorrectLoginException() {
        super("User not found, or password does not match.");
    }

}
