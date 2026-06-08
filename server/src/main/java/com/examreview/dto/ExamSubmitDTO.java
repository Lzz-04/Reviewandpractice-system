package com.examreview.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class ExamSubmitDTO {
    @NotNull(message = "考试记录ID不能为空")
    private Integer recordId;
    private List<AnswerItem> answers;

    @Data
    public static class AnswerItem {
        @NotNull(message = "题目ID不能为空")
        private Integer questionId;
        private String selectedAnswer;
    }
}
