package com.examreview.controller;

import com.examreview.dto.AIGenerateRequest;
import com.examreview.dto.AIGeneratedQuestion;
import com.examreview.dto.ApiResponse;
import com.examreview.service.AIService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;

    /**
     * AI 出题 — 生成题目预览（不持久化，前端确认后再保存）
     */
    @PostMapping("/generate")
    public ApiResponse<List<AIGeneratedQuestion>> generate(@RequestBody @Valid AIGenerateRequest request) {
        List<AIGeneratedQuestion> questions = aiService.generate(request);
        return ApiResponse.ok(questions, "AI 已生成 " + questions.size() + " 道题目");
    }
}
