package com.suken27.humanfactorsjava.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class HumanFactorFactoryTest {

    @Autowired
    private HumanFactorFactory humanFactorFactory;

    @Test
    void testGetAll() {
        List<HumanFactorType> all = humanFactorFactory.getAll();
        assertNotNull(all);
        for (HumanFactorType humanFactorType : all) {
            assertNotNull(humanFactorType);
            assertNotNull(humanFactorType.getTitle());
            assertNotNull(humanFactorType.getQuestionTypes());
            assertNotNull(humanFactorType.getActionTypes());
            assertNotNull(humanFactorType.getAffectsTo());
            assertNotNull(humanFactorType.getBibliographicSource());
            if (humanFactorType.getQuestionTypes().size() > 0) {
                for (QuestionType questionType : humanFactorType.getQuestionTypes()) {
                    assertNotNull(questionType);
                    assertNotNull(questionType.getQuestionText());
                }
            }
            if (humanFactorType.getActionTypes().size() > 0) {
                for (ActionType actionType : humanFactorType.getActionTypes()) {
                    assertNotNull(actionType);
                    assertNotNull(actionType.getTitle());
                }
            }
            if (humanFactorType.getAffectsTo().size() > 0) {
                for (HumanFactorType dependency : humanFactorType.getAffectsTo()) {
                    assertNotNull(dependency);
                    assertNotNull(dependency.getTitle());
                }
            }
            if (humanFactorType.getBibliographicSource().size() > 0) {
                for (BibliographicSource bibliographicSource : humanFactorType.getBibliographicSource()) {
                    assertNotNull(bibliographicSource);
                    assertNotNull(bibliographicSource.getTitle());
                }
            }
        }
    }
}
