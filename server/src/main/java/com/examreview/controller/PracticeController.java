package com.examreview.controller;

import com.examreview.dto.ApiResponse;
import com.examreview.dto.PracticeAnswerDTO;
import com.examreview.dto.WrongQuestionDTO;
import com.examreview.entity.AnswerRecord;
import com.examreview.entity.Question;
import com.examreview.mapper.AnswerRecordMapper;
import com.examreview.mapper.ChapterMapper;
import com.examreview.service.QuestionService;
import com.examreview.service.WrongBookService;
import com.examreview.util.AnswerChecker;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/practice")
@RequiredArgsConstructor
public class PracticeController {

    private final QuestionService questionService;
    private final WrongBookService wrongBookService;
    private final AnswerRecordMapper answerRecordMapper;
    private final ChapterMapper chapterMapper;

    @GetMapping("/start/{chapterId}")
    public ApiResponse<Map<String, Object>> startPractice(
            @PathVariable Integer chapterId,
            @RequestParam(defaultValue = "sequential") String mode,
            @RequestParam(defaultValue = "false") boolean wrongOnly) {
        // 校验章节存在
        if (chapterMapper.selectById(chapterId) == null) {
            return ApiResponse.fail("章节不存在");
        }
        List<Question> questions;
        if (wrongOnly) {
            List<WrongQuestionDTO> wrongList = wrongBookService.getList(1, 10000, null, 0).getRecords();
            Set<Integer> wrongChapterIds = wrongList.stream()
                    .map(WrongQuestionDTO::getChapterId).collect(Collectors.toSet());
            if (!wrongChapterIds.contains(chapterId)) {
                questions = Collections.emptyList();
            } else {
                questions = wrongList.stream()
                        .filter(w -> w.getChapterId().equals(chapterId))
                        .map(this::toQuestion)
                        .collect(Collectors.toList());
                if (questions.size() > 50) {
                    questions = questions.subList(0, 50);
                }
            }
        } else {
            questions = questionService.getRandomQuestions(chapterId, 50);
        }
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

    @GetMapping({"/wrong/{subjectId}", "/wrong"})
    public ApiResponse<Map<String, Object>> startWrongPractice(
            @PathVariable(required = false) Integer subjectId,
            @RequestParam(defaultValue = "false") boolean unMasteredOnly) {
        List<WrongQuestionDTO> wrongList = wrongBookService.getList(1, 10000, subjectId, unMasteredOnly ? 0 : null).getRecords();
        List<Question> questions = wrongList.stream()
                .map(this::toQuestion)
                .collect(Collectors.toList());
        if (questions.size() > 50) {
            questions = questions.subList(0, 50);
        }
        Collections.shuffle(questions);
        String sessionId = UUID.randomUUID().toString();
        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("questions", questions);
        result.put("total", questions.size());
        return ApiResponse.ok(result);
    }

    private Question toQuestion(WrongQuestionDTO dto) {
        Question q = new Question();
        q.setId(dto.getQuestionId());
        q.setChapterId(dto.getChapterId());
        q.setSubjectId(dto.getSubjectId());
        q.setType(dto.getType());
        q.setContent(dto.getContent());
        q.setOptions(dto.getOptions());
        q.setAnswer(dto.getAnswer());
        q.setAnalysis(dto.getAnalysis());
        q.setDifficulty(dto.getDifficulty());
        return q;
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
