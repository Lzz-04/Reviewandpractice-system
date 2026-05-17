package com.examreview.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("exam_records")
public class ExamRecord {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer examId;
    private Integer subjectId;
    private BigDecimal score;
    private Integer totalQuestions;
    private Integer correctCount;
    private Integer wrongCount;
    private Integer durationUsed;
    private Integer durationRemaining;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private LocalDateTime createdAt;
}
