package com.suken27.humanfactorsjava.model.exception;

public class QuestionNotFoundException extends RuntimeException {
    
    public QuestionNotFoundException(Long id) {
        super("Could not find question with id " + id);
    }

}
