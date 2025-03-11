package com.suken27.humanfactorsjava.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Data
@EqualsAndHashCode(exclude = {"affectsTo", "affectedBy", "actions"})
@ToString(exclude = {"affectsTo", "affectedBy", "actions"})
public class TeamHumanFactor {
    
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne
    private HumanFactorType type;
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<TeamHumanFactor> affectsTo;
    @ManyToMany(fetch = FetchType.EAGER)
    private List<TeamHumanFactor> affectedBy;
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Action> actions;
    private boolean isFullyMeasured;
    private Double score;

    /**
     * This constructor should never be used. Use TeamHumanFactor(HumanFactorType) instead.
     */
    public TeamHumanFactor() {
        super();
    }

    public TeamHumanFactor(HumanFactorType type) {
        this.type = type;
        affectsTo = new ArrayList<>();
        affectedBy = new ArrayList<>();
    }

}
