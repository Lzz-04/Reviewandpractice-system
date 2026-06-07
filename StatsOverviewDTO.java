package com.examreview.dto;

import lombok.Data;

@Data
public class StatsOverviewDTO {
    private Long totalQuestions;
    private Long totalAnswered;
    private Double overallAccuracy;
    private Long todayAnswered;
    private Integer studyDays;
}
