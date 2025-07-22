package com.suken27.humanfactorsjava.rest.advice;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import com.suken27.humanfactorsjava.model.exception.OnlyOneUserAllowedException;

@ControllerAdvice
public class OnlyOneUserAllowedAdvice {

    @ResponseBody
    @ExceptionHandler(OnlyOneUserAllowedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String onlyOneUserAllowedAdvice(OnlyOneUserAllowedException exception) {
        return exception.getMessage();
    }
}