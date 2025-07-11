package com.suken27.humanfactorsjava.rest.advice;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.suken27.humanfactorsjava.rest.exception.IncorrectEmailFormatException;

@ControllerAdvice
public class IncorrectEmailFormatAdvice {
    
    @ResponseBody
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String incorrectEmailFormatAdvice(IncorrectEmailFormatException exception) {
        return exception.getMessage();
    }

}
