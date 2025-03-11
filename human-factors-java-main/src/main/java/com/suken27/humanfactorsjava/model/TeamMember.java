package com.suken27.humanfactorsjava.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"team"})
public class TeamMember extends User {

    @Column(nullable = false)
    private boolean deleted;

    private LocalDateTime deletionTime;

    @ManyToOne(fetch = FetchType.LAZY)
    private Team team;
    
    /**
     * This constructor should never be used. Use TeamMember(List<HumanFactor>) instead.
     * This constructor cannot be removed as hibernate uses the default constructor to instantiate entities.
     */
    public TeamMember() {
        super();
    }

    public TeamMember(HumanFactorFactory humanFactorFactory) {
        super(humanFactorFactory.createInstances());
        deleted = false;
    }
    
}
