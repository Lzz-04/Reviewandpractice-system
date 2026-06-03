package com.examreview.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("subjects")
public class Subject {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String description;
    private String icon;
    private Integer sortOrder;
    private Long userId;
    private LocalDateTime createdAt;
}
