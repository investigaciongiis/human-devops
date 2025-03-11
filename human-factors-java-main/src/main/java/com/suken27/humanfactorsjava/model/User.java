package com.suken27.humanfactorsjava.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
// The word 'user' is a reserved keyword for H2 databases, so it needs to be
// replaced.
@Table(name = "Users")
@Data
@Slf4j
public abstract class User {

    @Id
    @GeneratedValue
    private Long id;
    @Column(nullable = false)
    private String email;
    private String slackId;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<HumanFactor> humanFactors;

    /**
     * This constructor should never be used. Use User(List<HumanFactor) instead.
     * This constructor cannot be removed as hibernate uses the default constructor
     * to instantiate entities.
     */
    protected User() {
        super();
    }

    protected User(List<HumanFactor> humanFactors) {
        super();
        this.humanFactors = humanFactors;
    }

    /**
     * Returns a random set of questions addressing some the less frequently
     * measured human factors. The questions are selected through a weighted random
     * selection (the less frequently measured human factors have a higher chance of
     * being selected).
     * 
     * @param numberOfQuestions Amount of questions to be returned.
     * @return A list of questions.
     * @see Question
     * @see HumanFactor
     */
    public List<Question> launchQuestions(int numberOfQuestions) {
        List<Question> launchQuestions = new ArrayList<>();
        List<Question> questions = new ArrayList<>();
        long totalWeight = 0;
        for (HumanFactor humanFactor : humanFactors) {
            Question oldestQuestion = humanFactor.getOldestQuestion();
            if (oldestQuestion != null) {
                questions.add(oldestQuestion);
                // The +1 is to avoid 0 total weight if the questions are launched the same day
                // they are created.
                totalWeight += oldestQuestion.daysSinceLastAnswer() + 1;
            }
        }
        double random;
        for (int i = 0; i < numberOfQuestions; i++) {
            random = Math.random() * totalWeight;
            for (Question question : questions) {
                random -= question.daysSinceLastAnswer() + 1;
                if (random <= 0.0d) {
                    launchQuestions.add(question);
                    questions.remove(question);
                    totalWeight -= question.daysSinceLastAnswer() + 1;
                    break;
                }
            }
        }
        return launchQuestions;
    }

    /**
     * 
     * @param questionId
     * @param answer
     * @return The human factor that was changed if it is fully measured, null otherwise.
     */
    public HumanFactor answerQuestion(Question question, Double answer) {
        HumanFactor humanFactor = getHumanFactorByQuestion(question);
        if(humanFactor == null) {
            log.error("Question [{}] does not belong to any human factor", question.getId());
            return null;
        }
        if(humanFactor.answerQuestion(question.getId(), answer) != null) {
            return humanFactor;
        }
        return null;
    }

    private HumanFactor getHumanFactorByQuestion(Question question) {
        for (HumanFactor humanFactor : humanFactors) {
            for (Question humanFactorQuestion : humanFactor.getQuestions()) {
                if (humanFactorQuestion.getId().equals(question.getId())) {
                    return humanFactor;
                }
            }
        }
        return null;
    }

}
