package com.suken27.humanfactorsjava.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.suken27.humanfactorsjava.model.TeamManager;

public interface TeamManagerRepository extends JpaRepository<TeamManager, Long> {

    public TeamManager findByEmail(String email);

}