package com.examreview.controller;

import com.examreview.dto.ApiResponse;
import com.examreview.dto.PracticeAnswerDTO;
import com.examreview.entity.AnswerRecord;
import com.examreview.entity.Question;
import com.examreview.mapper.AnswerRecordMapper;
import com.examreview.service.QuestionService;
import com.examreview.service.WrongBookService;
import com.examreview.util.AnswerChecker;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/practice")
@RequiredArgsConstructor
public class PracticeController {

    private final QuestionService questionService;
    private final WrongBookService wrongBookService;
    private final AnswerRecordMapper answerRecordMapper;

    @GetMapping("/start/{chapterId}")
    public ApiResponse<Map<String, Object>> startPractice(
            @PathVariable Integer chapterId,
            @RequestParam(defaultValue = "sequential") String mode) {
        List<Question> questions = questionService.getRandomQuestions(chapterId, 50);
        if ("random".equals(mode)) {
            Collections.shuffle(questions);
        }
        String sessionId = UUID.randomUUID().toString();
        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("questions", questions);
        result.put("total", questions.size());
        return ApiResponse.ok(result);
    }

    @PostMapping("/answer")
    public ApiResponse<Map<String, Object>> submitAnswer(@RequestBody @Valid PracticeAnswerDTO dto) {
        Question question = questionService.getById(dto.getQuestionId());
        boolean isCorrect = AnswerChecker.checkAnswer(question, dto.getSelectedAnswer());

        if (!isCorrect) {
            wrongBookService.upsertWrongQuestion(question);
        }

        // 保存答题记录，用于学习进度统计
        AnswerRecord ar = new AnswerRecord();
        ar.setQuestionId(question.getId());
        ar.setSessionId(dto.getSessionId());
        ar.setSelectedAnswer(dto.getSelectedAnswer());
        ar.setIsCorrect(isCorrect ? 1 : 0);
        ar.setTimeSpent(dto.getTimeSpent());
        ar.setAnsweredAt(LocalDateTime.now());
        answerRecordMapper.insert(ar);

        Map<String, Object> result = new HashMap<>();
        result.put("questionId", dto.getQuestionId());
        result.put("isCorrect", isCorrect);
        result.put("correctAnswer", question.getAnswer());
        result.put("analysis", question.getAnalysis());
        return ApiResponse.ok(result);
    }

}
