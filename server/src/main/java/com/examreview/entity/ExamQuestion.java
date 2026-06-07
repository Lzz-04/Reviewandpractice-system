package com.examreview.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("exam_questions")
public class ExamQuestion {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer examId;
    private Integer questionId;
    private Integer sortOrder;
}
