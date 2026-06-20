package com.examreview.service;

import com.examreview.dto.AIGenerateRequest;
import com.examreview.dto.AIGeneratedQuestion;

import java.util.List;

public interface AIService {

    /**
     * 调用 AI 生成题目（仅返回预览，不持久化）
     * @param request 生成请求参数
     * @param userId 当前用户ID（用于获取已有题库作为参考样例）
     */
    List<AIGeneratedQuestion> generate(AIGenerateRequest request, Long userId);

    /**
     * 调用 AI 分析文本（通用方法）
     */
    String analyze(String systemPrompt, String userPrompt);
}
