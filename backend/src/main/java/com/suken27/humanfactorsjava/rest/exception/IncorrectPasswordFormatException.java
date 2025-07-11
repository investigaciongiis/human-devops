package com.suken27.humanfactorsjava.rest.exception;

public class IncorrectPasswordFormatException extends RuntimeException {
    
    public IncorrectPasswordFormatException() {
        super("The password introduced does not have a valid format.");
    }

}
