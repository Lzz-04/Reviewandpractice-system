package com.examreview.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("questions")
public class Question {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer subjectId;
    private String type;
    private String content;
    private String options = "[]";
    private String answer;
    private String analysis;
    private Integer difficulty;
    private Long userId;
    private LocalDateTime createdAt;
}
