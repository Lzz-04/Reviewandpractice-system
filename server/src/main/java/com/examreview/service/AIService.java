package com.examreview.service;

import com.examreview.dto.AIGenerateRequest;
import com.examreview.dto.AIGeneratedQuestion;

import java.util.List;

public interface AIService {

    /**
     * 调用 AI 生成题目（仅返回预览，不持久化）
     */
    List<AIGeneratedQuestion> generate(AIGenerateRequest request);
}
