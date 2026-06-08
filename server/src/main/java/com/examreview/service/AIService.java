package com.examreview.service;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class AIService {

    /**
     * 调用 AI 生成题目
     * @param subjectId 科目ID
     * @param chapterId 章节ID
     * @param type 题型
     * @param count 数量
     * @return 题目列表
     */
    public List<Map<String, Object>> generateQuestions(Long subjectId, Long chapterId, String type, int count) {
        // 调用 DeepSeek API
        // 使用 Spring WebClient
        return List.of();
    }
}
