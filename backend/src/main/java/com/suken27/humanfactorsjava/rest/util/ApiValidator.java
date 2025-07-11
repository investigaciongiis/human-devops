package com.suken27.humanfactorsjava.rest.util;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

@Service
public class ApiValidator {
    
    private static final String EMAIL_REGEX = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public ApiValidator() {
        super();
    }

    public boolean isValidEmail(String email) {
        if(email == null) {
            return false;
        }
        return Pattern.compile(EMAIL_REGEX).matcher(email).matches();
    }

    public boolean isValidPassword(String password) {
        if(password == null) {
            return false;
        }
        return password.length() > 5;
    }

    public LocalTime parseTimeString(String time) {
        LocalTime localTime;
        try {
            localTime = LocalTime.parse(time, dateTimeFormatter);
        } catch(DateTimeParseException e) {
            return null;
        }
        return localTime;
    }

}
