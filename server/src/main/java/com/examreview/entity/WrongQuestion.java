package com.examreview.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("wrong_questions")
public class WrongQuestion {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer questionId;
    private Integer subjectId;
    private Integer chapterId;
    private Integer wrongCount;
    private LocalDateTime lastWrongAt;
    private Integer reviewedCount;
    private Integer mastered;
    private Long userId;
    private LocalDateTime createdAt;
}
