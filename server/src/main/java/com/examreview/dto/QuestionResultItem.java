package com.examreview.dto;

import lombok.Data;

@Data
public class QuestionResultItem {
    private Integer questionId;
    private Integer questionIndex;
    private String type;
    private String content;
    private String options;
    private String correctAnswer;
    private String selectedAnswer;
    private Boolean isCorrect;
    private String analysis;
}
