package com.examreview.controller;

import com.examreview.service.WrongBookService;
import com.examreview.util.AnswerChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/practice")
public class PracticeController {

    @Autowired
    private WrongBookService wrongBookService;

    @Autowired
    private AnswerChecker answerChecker;

    @GetMapping("/start/{chapterId}")
    public List<Object> startPractice(@PathVariable Long chapterId, @RequestParam(defaultValue = "sequential") String mode) {
        // 获取章节题目列表，支持顺序或随机
        return null;
    }

    @GetMapping("/wrong/{subjectId}")
    public List<Object> startWrongPractice(@PathVariable Long subjectId) {
        // 获取科目下的错题练习（上限 50）
        return null;
    }

    @PostMapping("/answer")
    public Map<String, Object> submitAnswer(@RequestBody Map<String, Object> request) {
        Long questionId = Long.valueOf(request.get("questionId").toString());
        String type = (String) request.get("type");
        String userAnswer = (String) request.get("userAnswer");
        String correctAnswer = (String) request.get("correctAnswer");
        Long userId = 1L; // 实际应从 SecurityContext 获取

        boolean isCorrect = answerChecker.check(type, userAnswer, correctAnswer);

        if (!isCorrect) {
            wrongBookService.upsertWrongQuestion(userId, questionId);
        }

        return Map.of(
            "isCorrect", isCorrect,
            "correctAnswer", correctAnswer,
            "analysis", "解析内容..."
        );
    }
}
