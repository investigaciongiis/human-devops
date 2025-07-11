package com.suken27.humanfactorsjava.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"team"})
public class TeamManager extends User {

	@Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private Role role = Role.USER;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "team_id")
	private Team team;

	/**
	 * This constructor should never be used. Use TeamManager(List<HumanFactor>) instead.
	 * This constructor cannot be removed as hibernate uses the default constructor to instantiate entities.
	 */
	public TeamManager() {
		super();
	}

	public TeamManager(HumanFactorFactory humanFactorFactory) {
		super(humanFactorFactory.createInstances());
		team = new Team(this, humanFactorFactory.createTeamInstances());
	}

}
