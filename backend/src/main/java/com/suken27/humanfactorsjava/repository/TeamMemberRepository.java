package com.suken27.humanfactorsjava.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.suken27.humanfactorsjava.model.TeamMember;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    
    @Query("Select m FROM TeamMember m WHERE m.email = ?1 AND m.deleted = false")
    public TeamMember findByEmail(String email);

    @Query("Select m FROM TeamMember m WHERE m.email = ?1")
    public List<TeamMember> findByEmailIncludingDeleted(String email);
    
}
