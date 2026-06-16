package com.examreview.dto;

import lombok.Data;
import java.util.Map;

/**
 * 暂停考试请求 DTO
 */
@Data
public class PauseExamDTO {

    /** 剩余秒数 */
    private Integer remainingSeconds = 0;

    /** 已答题目 key=questionId, value=selectedAnswer */
    private Map<Integer, String> answers;

    /** 当前题目序号（从0开始） */
    private Integer currentIndex = 0;
}
