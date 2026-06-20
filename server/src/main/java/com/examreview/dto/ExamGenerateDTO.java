package com.examreview.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExamGenerateDTO {
    @NotNull(message = "请选择科目")
    private Integer subjectId;
    @NotBlank(message = "请输入考试标题")
    private String title;
    @NotNull(message = "请输入考试时长")
    @Min(value = 1, message = "考试时长至少1分钟")
    private Integer duration;
    @NotNull(message = "请输入题目数量")
    @Min(value = 1, message = "题目数量至少1道")
    private Integer totalCount;
}
