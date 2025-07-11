package com.suken27.humanfactorsjava.model.exception;

public class HumanFactorNotFoundException extends RuntimeException {

    public HumanFactorNotFoundException(Long id) {
        super("Could not find human factor with id: '" + id + "'");
    }

}
