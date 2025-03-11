package com.suken27.humanfactorsjava.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.suken27.humanfactorsjava.model.Question;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    
}
