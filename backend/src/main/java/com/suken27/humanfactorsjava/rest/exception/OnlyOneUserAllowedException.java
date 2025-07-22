package com.suken27.humanfactorsjava.model.exception;

public class OnlyOneUserAllowedException extends RuntimeException {
    public OnlyOneUserAllowedException() {
        super("User registration is restricted to a single user.");
    }
}