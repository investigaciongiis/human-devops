package com.suken27.humanfactorsjava.model;

import java.util.Map;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

/**
 * Flyweight class that registers the intrinsic state of a Question.
 * @see Question
 */
@Entity
@Data
public class QuestionType {
    
    @Id
    private Long id;
    private String questionText;
    private boolean onlyForManagement;
    private boolean isNegative;
    private TypeOfAnswer typeOfAnswer;

    public QuestionType() {
        super();
    }

    /**
     * Creates an instance of question with this type as intrinsic state.
     * @return Question (extrinsic state) with this object as intrinsic state.
     */
    public Question createInstance() {
        return new Question(this);
    }

    public String getAnswerText(Double answer) {
        Map<Double, String> options = typeOfAnswer.getOptions();
        return options.get(answer);
    }

}
