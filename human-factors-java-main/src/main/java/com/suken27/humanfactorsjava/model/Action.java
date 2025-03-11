package com.suken27.humanfactorsjava.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class Action {

    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne
    private ActionType type;

    /**
     * This constructor should never be used. Use Action(ActionType) instead.
	 * This constructor cannot be removed as hibernate uses the default constructor to instantiate entities.
     */
    public Action() {
        super();
    }

    public Action(ActionType actionType) {
        super();
        type = actionType;
    }
    
}
