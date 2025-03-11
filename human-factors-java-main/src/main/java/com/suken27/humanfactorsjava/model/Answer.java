package com.suken27.humanfactorsjava.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Answer {
    
    @Id
    @GeneratedValue
    private Long id;
    private LocalDateTime creationTime;
    private Double answerValue;

}
