package com.suken27.humanfactorsjava.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.suken27.humanfactorsjava.repository.HumanFactorTypeRepository;

@Component
public class HumanFactorFactory {

    @Autowired
    private HumanFactorTypeRepository humanFactorTypeRepository;
    
    private List<HumanFactorType> all;

    public List<HumanFactorType> getAll() {
        if(all == null) {
            all = humanFactorTypeRepository.findAll();
            List<HumanFactorType> actions = humanFactorTypeRepository.findAllFetchActions(all);
            List<HumanFactorType> questions = humanFactorTypeRepository.findAllFetchQuestions(all);
            List<HumanFactorType> dependsOn = humanFactorTypeRepository.findAllFetchAffectsTo(all);
            List<HumanFactorType> bibliographicSource = humanFactorTypeRepository.findAllFetchBibliographicSource(all);
            for(int i = 0; i < all.size(); i++) {
                HumanFactorType humanFactorType = all.get(i);
                humanFactorType.setActionTypes(actions.get(i).getActionTypes());
                humanFactorType.setQuestionTypes(questions.get(i).getQuestionTypes());
                humanFactorType.setAffectsTo(dependsOn.get(i).getAffectsTo());
                humanFactorType.setBibliographicSource(bibliographicSource.get(i).getBibliographicSource());
            }
        }
        
        return all;
    }

    public List<HumanFactor> createInstances() {
        List<HumanFactor> humanFactors = new ArrayList<>();
        for (HumanFactorType humanFactorType : getAll()) {
            humanFactors.add(humanFactorType.createInstance());
        }
        return humanFactors;
    }

    public List<TeamHumanFactor> createTeamInstances() {
        Map<HumanFactorType, TeamHumanFactor> map = new HashMap<>();
        Map<ActionType, Action> sharedActions = new HashMap<>();
        List<TeamHumanFactor> humanFactors = new ArrayList<>();
        for (HumanFactorType humanFactorType : getAll()) {
            TeamHumanFactor humanFactor = humanFactorType.createTeamInstance(sharedActions);
            map.put(humanFactorType, humanFactor);
            humanFactors.add(humanFactor);
        }
        // Adds the depending factors objects with aliasing
        for (TeamHumanFactor humanFactor : humanFactors) {
            for (HumanFactorType humanFactorType : humanFactor.getType().getAffectsTo()) {
                TeamHumanFactor affectedBy = map.get(humanFactorType);
                humanFactor.getAffectsTo().add(affectedBy);
                affectedBy.getAffectedBy().add(humanFactor);
            }
        }
        return humanFactors;
    }

}
