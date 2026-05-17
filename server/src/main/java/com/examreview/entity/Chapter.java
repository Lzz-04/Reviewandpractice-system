package com.examreview.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("chapters")
public class Chapter {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer subjectId;
    private String name;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
