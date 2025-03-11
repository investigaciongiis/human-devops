package com.suken27.humanfactorsjava.rest.advice;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.suken27.humanfactorsjava.rest.exception.IncorrectPasswordFormatException;

@ControllerAdvice
public class IncorrectPasswordFormatAdvice {
    
    @ResponseBody
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String incorrectEmailFormatAdvice(IncorrectPasswordFormatException exception) {
        return exception.getMessage();
    }

}
