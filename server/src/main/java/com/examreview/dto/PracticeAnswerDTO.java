package com.examreview.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PracticeAnswerDTO {
    @NotNull(message = "题目ID不能为空")
    private Integer questionId;
    private String sessionId;
    private String selectedAnswer;
    private Integer timeSpent;
}
