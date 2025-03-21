package com.suken27.humanfactorsjava.rest.advice;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.suken27.humanfactorsjava.model.exception.EmailInUseException;

@ControllerAdvice
public class EmailInUseAdvice {
 
    @ResponseBody
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String incorrectEmailFormatAdvice(EmailInUseException exception) {
        return exception.getMessage();
    }

}
