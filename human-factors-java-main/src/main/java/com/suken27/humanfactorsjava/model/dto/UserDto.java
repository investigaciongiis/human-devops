package com.suken27.humanfactorsjava.model.dto;

import com.suken27.humanfactorsjava.model.User;

import lombok.Data;

@Data
public class UserDto {
    
    private Long id;
    private String email;
    private String slackId;

    protected UserDto() {
        super();
    }

    public UserDto(User entity) {
        setId(entity.getId());
        setEmail(entity.getEmail());
        setSlackId(entity.getSlackId());
    }

}
