package com.suken27.humanfactorsjava.rest.advice;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.suken27.humanfactorsjava.model.exception.TeamManagerNotFoundException;

@ControllerAdvice
public class TeamManagerNotFoundAdvice {
    
    @ResponseBody
    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String incorrectEmailFormatAdvice(TeamManagerNotFoundException exception) {
        return exception.getMessage();
    }

}
