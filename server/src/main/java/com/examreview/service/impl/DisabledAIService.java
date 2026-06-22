package com.examreview.service.impl;

import com.examreview.dto.AIGenerateRequest;
import com.examreview.dto.AIGeneratedQuestion;
import com.examreview.exception.BusinessException;
import com.examreview.service.AIService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI 功能禁用时的 fallback 实现，避免其他控制器因缺少 AIService Bean 而启动失败。
 * 当 deepseek.enabled=false 或未配置时自动启用。
 */
@Service
@ConditionalOnProperty(name = "deepseek.enabled", havingValue = "false", matchIfMissing = true)
public class DisabledAIService implements AIService {

    private static final String DISABLED_MSG = "AI 功能未启用，请在 application.yml 中设置 deepseek.enabled=true 并配置有效的 API Key";

    @Override
    public List<AIGeneratedQuestion> generate(AIGenerateRequest request, Long userId) {
        throw new BusinessException(DISABLED_MSG);
    }

    @Override
    public String analyze(String systemPrompt, String userPrompt) {
        throw new BusinessException(DISABLED_MSG);
    }
}
