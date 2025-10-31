package com.suken27.humanfactorsjava.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionScheduleDto {
    private String questionSendingTime;   // "09:00"
    private String questionFrequency;     // "DAILY" | "WEEKLY" | "MONTHLY"
    private String questionDayOfWeek;     // "MONDAY" | "TUESDAY" ...
    private Integer questionDayOfMonth;   // 1..28
}