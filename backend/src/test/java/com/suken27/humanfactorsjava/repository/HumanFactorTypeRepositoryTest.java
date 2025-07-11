package com.suken27.humanfactorsjava.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.suken27.humanfactorsjava.model.ActionType;
import com.suken27.humanfactorsjava.model.BibliographicSource;
import com.suken27.humanfactorsjava.model.HumanFactorType;
import com.suken27.humanfactorsjava.model.QuestionType;

@SpringBootTest
public class HumanFactorTypeRepositoryTest {

    @Autowired
    private HumanFactorTypeRepository repository;

    private static final Logger logger = LoggerFactory.getLogger(HumanFactorTypeRepositoryTest.class);

    @Test
    public void testContent() {
        List<HumanFactorType> entities = repository.findAll();
        entities = repository.findAllFetchAffectsTo(entities);
        assertNotNull(entities);
        List<HumanFactorType> dependencies;
        for (HumanFactorType entity : entities) {
            assertNotNull(entity);
            dependencies = entity.getAffectsTo();
            assertNotNull(dependencies);
            if(dependencies.size() > 0) {
                for (HumanFactorType dependency : dependencies) {
                    logger.debug("{} -> {}", entity.getTitle(), dependency.getTitle());
                }
            }
        }
        assertNotEquals(0, entities.size());
    }

    @Test
    void testFindAllFetchActions() {
        List<HumanFactorType> entities = repository.findAll();
        assertNotNull(entities);
        entities = repository.findAllFetchActions(entities);
        assertNotNull(entities);
        for(HumanFactorType humanFactorType : entities) {
            assertNotNull(humanFactorType);
            assertNotNull(humanFactorType.getActionTypes());
            if(humanFactorType.getActionTypes().size() > 0) {
                for(ActionType actionType : humanFactorType.getActionTypes()) {
                    assertNotNull(actionType);
                    assertNotNull(actionType.getTitle());
                }
            }
        }
    }

    @Test
    void testFindAllFetchBibliographicSource() {
        List<HumanFactorType> entities = repository.findAll();
        assertNotNull(entities);
        entities = repository.findAllFetchBibliographicSource(entities);
        assertNotNull(entities);
        for(HumanFactorType humanFactorType : entities) {
            assertNotNull(humanFactorType);
            assertNotNull(humanFactorType.getBibliographicSource());
            if(humanFactorType.getBibliographicSource().size() > 0) {
                for(BibliographicSource bibliographicSource : humanFactorType.getBibliographicSource()) {
                    assertNotNull(bibliographicSource);
                    assertNotNull(bibliographicSource.getTitle());
                }
            }
        }
    }

    @Test
    void testFindAllFetchDependsOn() {
        List<HumanFactorType> entities = repository.findAll();
        assertNotNull(entities);
        entities = repository.findAllFetchAffectsTo(entities);
        assertNotNull(entities);
        for(HumanFactorType humanFactorType : entities) {
            assertNotNull(humanFactorType);
            assertNotNull(humanFactorType.getAffectsTo());
            if(humanFactorType.getAffectsTo().size() > 0) {
                for(HumanFactorType depends : humanFactorType.getAffectsTo()) {
                    assertNotNull(depends);
                    assertNotNull(depends.getTitle());
                }
            }
        }
    }

    @Test
    void testFindAllFetchQuestions() {
        List<HumanFactorType> entities = repository.findAll();
        assertNotNull(entities);
        entities = repository.findAllFetchQuestions(entities);
        assertNotNull(entities);
        for(HumanFactorType humanFactorType : entities) {
            assertNotNull(humanFactorType);
            assertNotNull(humanFactorType.getQuestionTypes());
            for(QuestionType questionType : humanFactorType.getQuestionTypes()) {
                assertNotNull(questionType);
                assertNotNull(questionType.getQuestionText());
            }
        }
    }

}
