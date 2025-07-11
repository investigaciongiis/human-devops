package com.suken27.humanfactorsjava.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Data;

@Entity
@Data
public class HumanFactor {
    
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne
    private HumanFactorType type;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Question> questions;
    private LocalDate lastQuestionAnswered;
    private boolean isFullyMeasured;
    private Double score;

    /**
     * This constructor should never be used. Use HumanFactor(HumanFactorType) instead.
	 * This constructor cannot be removed as hibernate uses the default constructor to instantiate entities.
     */
    public HumanFactor() {
        super();
    }

    public HumanFactor(HumanFactorType humanFactorType) {
        super();
        type = humanFactorType;
        questions = new ArrayList<>();
        for(QuestionType questionType : humanFactorType.getQuestionTypes()) {
            questions.add(questionType.createInstance());
        }
        isFullyMeasured = false;
    }

    public long oldestQuestionDaysSinceLastAnswer() {
        Question question = getOldestQuestion();
        if(question == null) {
            return 0;
        }
        return getOldestQuestion().daysSinceLastAnswer();
    }

    public Question getOldestQuestion() {
        if(questions == null || questions.isEmpty()) {
            return null;
        }
        Question oldestQuestion = questions.get(0);
        for (Question question : questions) {
            if(question.getLastAnswerDateTime() == null) {
                return question;
            }
            if(question.getLastAnswerDateTime().isBefore(oldestQuestion.getLastAnswerDateTime())) {
                oldestQuestion = question;
            }
        }
        return oldestQuestion;
    }

    /**
     * 
     * @param questionId
     * @param answer
     * @return The score of the human factor if all the questions have been answered, null otherwise.
     */
    public Double answerQuestion(Long questionId, Double answer) {
        for (Question question : questions) {
            if(question.getId().equals(questionId)) {
                question.answer(answer);
                updateHumanFactor();
                if(isFullyMeasured) {
                    return score;
                }
                return null;
            }
        }
        return null;
    }

    public boolean canBeMeasured() {
        return !questions.isEmpty();
    }

    private void updateHumanFactor() {
        if(questions == null || questions.isEmpty()) {
            return;
        }
        isFullyMeasured = true;
        for (Question question : questions) {
            if(!question.isAnswered()) {
                isFullyMeasured = false;
                break;
            }
        }
        if(isFullyMeasured) {
            score = 0.0d;
            for (Question question : questions) {
                score += question.getScore();
            }
            score /= questions.size();
        }
    }

}
