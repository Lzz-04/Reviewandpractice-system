package com.examreview.dto;

import com.examreview.entity.ExamRecord;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 恢复考试响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumeExamResponse {

    /** 考试记录 */
    private ExamRecord record;

    /** 已保存的答题进度 key=questionId, value=selectedAnswer */
    private Map<Integer, String> savedAnswers;
}
