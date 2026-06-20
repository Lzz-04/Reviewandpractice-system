package com.examreview.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WrongQuestionDTO {
    // WrongQuestion 字段
    private Integer id;
    private Integer questionId;
    private Integer subjectId;
    private Integer wrongCount;
    private LocalDateTime lastWrongAt;
    private Integer reviewedCount;
    private Integer mastered;

    // Question 字段
    private String type;
    private String content;
    private String options;
    private String answer;
    private String analysis;
    private Integer difficulty;
}
