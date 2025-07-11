package com.suken27.humanfactorsjava.model.dto;

import com.suken27.humanfactorsjava.model.TeamManager;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TeamManagerDto extends UserDto {
    
    private Long team;
    private String password;

    public TeamManagerDto(TeamManager entity) {
        super(entity);
        setTeam(entity.getTeam().getId());
        setPassword(entity.getPassword());
    }

}
