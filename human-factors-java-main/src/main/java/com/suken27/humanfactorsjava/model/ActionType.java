package com.suken27.humanfactorsjava.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import lombok.Data;

/**
 * Flyweight class that registers the intrinsic state of an Action.
 * @see Action
 */
@Entity
@Data
public class ActionType {

    @Id
    private Long id;
    @Column(unique = true)
    private String title;
    @Lob
    private String description;
    @ManyToOne
    private BibliographicSource bibliographicSource;

    /**
     * Creates an instance of action with this type as intrinsic state.
     * @return Action (extrinsic state) with this object as intrinsic state.
     */
    public Action createInstance() {
        return new Action(this);
    }

}
