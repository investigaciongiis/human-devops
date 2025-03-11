package com.suken27.humanfactorsjava.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.suken27.humanfactorsjava.model.Team;

public interface TeamRepository extends JpaRepository<Team, Long> {

    @Query("Select t FROM Team t left join fetch t.members, TeamManager m WHERE t.manager = m AND m.email = ?1")
    public Team findByTeamManagerEmail(String email);

    @Query("Select t FROM Team t left join fetch t.members, TeamManager m WHERE t.manager = m AND m.id = ?1")
    public Team findByTeamManagerId(Long id);

    @Query("Select t FROM Team t LEFT JOIN FETCH t.members mem LEFT JOIN FETCH t.manager man WHERE man.email = ?1 OR mem.email = ?1")
    public Team findByMemberEmail(String email);

}
