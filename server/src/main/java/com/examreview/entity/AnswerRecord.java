package com.examreview.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("answer_records")
public class AnswerRecord {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer questionId;
    private Integer examId;
    private String sessionId;
    private String selectedAnswer;
    private Integer isCorrect;
    private LocalDateTime answeredAt;
    private Integer timeSpent;
}
