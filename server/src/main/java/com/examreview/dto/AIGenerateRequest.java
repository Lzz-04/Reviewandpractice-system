package com.examreview.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AIGenerateRequest {

    @NotNull(message = "章节ID不能为空")
    private Integer chapterId;

    @NotNull(message = "科目ID不能为空")
    private Integer subjectId;

    @NotBlank(message = "科目名称不能为空")
    private String subjectName;

    @NotBlank(message = "章节名称不能为空")
    private String chapterName;

    /** 题型：single/multiple/judge，null 或 "mixed" 表示混合 */
    private String type;

    @Min(value = 1, message = "难度最小为1")
    private Integer difficulty = 3;

    @Min(value = 1, message = "出题数量至少为1")
    private Integer count = 5;
}
