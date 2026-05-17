package com.examreview.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("exam_papers")
public class ExamPaper {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer subjectId;
    private String title;
    private Integer duration;
    private Integer questionCount;
    private LocalDateTime createdAt;
}
