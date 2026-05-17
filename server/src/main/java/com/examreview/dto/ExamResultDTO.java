package com.examreview.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExamResultDTO {
    private Integer id;
    private Integer examId;
    private String examTitle;
    private String subjectName;
    private BigDecimal score;
    private Integer totalQuestions;
    private Integer correctCount;
    private Integer wrongCount;
    private Integer durationUsed;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private List<QuestionResultItem> questions;
}
