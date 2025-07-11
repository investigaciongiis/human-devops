package com.suken27.humanfactorsjava.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.suken27.humanfactorsjava.model.HumanFactorType;

public interface HumanFactorTypeRepository extends JpaRepository<HumanFactorType, Long> {

    @Query("Select hft from HumanFactorType hft left join fetch hft.questionTypes where hft in ?1")
    public List<HumanFactorType> findAllFetchQuestions(List<HumanFactorType> humanFactorTypes);

    @Query("Select hft from HumanFactorType hft left join fetch hft.actionTypes where hft in ?1")
    public List<HumanFactorType> findAllFetchActions(List<HumanFactorType> humanFactorTypes);

    @Query("Select hft from HumanFactorType hft left join fetch hft.affectsTo where hft in ?1")
    public List<HumanFactorType> findAllFetchAffectsTo(List<HumanFactorType> humanFactorTypes);

    @Query("Select hft from HumanFactorType hft left join fetch hft.bibliographicSource where hft in ?1")
    public List<HumanFactorType> findAllFetchBibliographicSource(List<HumanFactorType> humanFactorTypes);
    
}
