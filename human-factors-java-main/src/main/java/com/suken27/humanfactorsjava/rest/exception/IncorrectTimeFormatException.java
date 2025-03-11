package com.suken27.humanfactorsjava.rest.exception;

public class IncorrectTimeFormatException extends RuntimeException {
    
    public IncorrectTimeFormatException(String time) {
        super("The string '" + time + "' does not represent a valid time format.");
    }

}
