package com.suken27.humanfactorsjava.model.dto;

import java.util.ArrayList;
import java.util.List;

import com.suken27.humanfactorsjava.model.TeamHumanFactor;

import lombok.Data;

@Data
public class HumanFactorDto {

    private Long id;
    private String title;
    private String description;
    private String cluster;
    private Double score;
    private boolean isFullyMeasured;
    private List<Long> affectsTo;
    private List<Long> affectedBy;

    public HumanFactorDto(TeamHumanFactor entity) {
        this.id = entity.getId();
        this.title = entity.getType().getTitle();
        this.description = entity.getType().getDescription();
        this.cluster = entity.getType().getCluster().name();
        this.score = entity.getScore();
        this.isFullyMeasured = entity.isFullyMeasured();
        this.affectedBy = new ArrayList<Long>();
        this.affectsTo = new ArrayList<Long>();
        for(TeamHumanFactor humanFactor : entity.getAffectsTo()) {
            this.affectsTo.add(humanFactor.getId());
        }
        for(TeamHumanFactor humanFactor : entity.getAffectedBy()) {
            this.affectedBy.add(humanFactor.getId());
        }
    }

    public static List<HumanFactorDto> toDto(List<TeamHumanFactor> entities) {
        return entities.stream().map(HumanFactorDto::new).toList();
    }
    
}
