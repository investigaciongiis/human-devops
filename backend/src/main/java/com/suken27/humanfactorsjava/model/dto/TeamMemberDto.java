package com.suken27.humanfactorsjava.model.dto;

import com.suken27.humanfactorsjava.model.TeamMember;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TeamMemberDto extends UserDto {
    
    private Long team;

    public TeamMemberDto() {
        super();
    }

    public TeamMemberDto(TeamMember entity) {
        super(entity);
        setTeam(entity.getTeam().getId());
        setSlackId(entity.getSlackId());
    }

}
