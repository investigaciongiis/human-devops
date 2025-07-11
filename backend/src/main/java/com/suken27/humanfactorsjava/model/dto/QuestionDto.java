package com.suken27.humanfactorsjava.model.dto;

import java.util.List;
import java.util.Map;

import com.suken27.humanfactorsjava.model.Question;

import lombok.Data;

@Data
public class QuestionDto {
    
    private Long id;
    private String questionText;
    private Map<Double, String> options;

    public QuestionDto(Question entity) {
        super();
        this.id = entity.getId();
        this.questionText = entity.getType().getQuestionText();
        this.options = entity.getType().getTypeOfAnswer().getOptions();
    }

    public static List<QuestionDto> toDto(List<Question> entities) {
        return entities.stream().map(QuestionDto::new).toList();
    }

}
