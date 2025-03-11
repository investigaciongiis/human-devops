package com.suken27.humanfactorsjava.model.dto;

import java.util.List;

import com.suken27.humanfactorsjava.model.Action;

import lombok.Data;

@Data
public class ActionDto {
    
    private Long id;
    private String title;
    private String description;
    private Double score;

    public ActionDto() {
        super();
    }

    public ActionDto(Action entity) {
        this.id = entity.getId();
        this.title = entity.getType().getTitle();
        this.description = entity.getType().getDescription();
    }

    public static List<ActionDto> toDto(List<Action> entities) {
        return entities.stream().map(ActionDto::new).toList();
    }

}
