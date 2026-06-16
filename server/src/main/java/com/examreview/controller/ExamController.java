package com.examreview.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.examreview.dto.*;
import com.examreview.entity.ExamPaper;
import com.examreview.entity.ExamRecord;
import com.examreview.service.ExamService;
import com.examreview.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.examreview.service.AIService;

import com.examreview.entity.Question;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;
    private final AIService aiService;

    @GetMapping
    public ApiResponse<Page<ExamPaper>> getList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Integer subjectId) {
        return ApiResponse.ok(examService.getList(page, pageSize, subjectId, SecurityUtil.getCurrentUserId()));
    }

    @GetMapping("/{id}")
    public ApiResponse<ExamPaper> getById(@PathVariable Integer id) {
        return ApiResponse.ok(examService.getById(id, SecurityUtil.getCurrentUserId()));
    }

    @PostMapping
    public ApiResponse<ExamPaper> create(@RequestBody ExamPaper examPaper) {
        return ApiResponse.ok(examService.create(examPaper, SecurityUtil.getCurrentUserId()), "创建成功");
    }

    @PostMapping("/generate")
    public ApiResponse<ExamPaper> generate(@RequestBody @Valid ExamGenerateDTO dto) {
        return ApiResponse.ok(examService.generate(dto, SecurityUtil.getCurrentUserId()), "组卷成功");
    }

    @PostMapping("/{id}/start")
    public ApiResponse<ExamRecord> startExam(@PathVariable Integer id) {
        return ApiResponse.ok(examService.startExam(id, SecurityUtil.getCurrentUserId()));
    }

    @PostMapping("/submit")
    public ApiResponse<ExamResultDTO> submitExam(@RequestBody @Valid ExamSubmitDTO dto) {
        return ApiResponse.ok(examService.submitExam(dto, SecurityUtil.getCurrentUserId()), "交卷成功");
    }

    @GetMapping("/records")
    public ApiResponse<Page<ExamRecord>> getRecords(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return ApiResponse.ok(examService.getRecords(page, pageSize, SecurityUtil.getCurrentUserId()));
    }

    /** 获取当前用户活跃的考试记录（进行中 + 已暂停） */
    @GetMapping("/records-active")
    public ApiResponse<List<ExamRecord>> getActiveRecords() {
        return ApiResponse.ok(examService.getActiveRecords(SecurityUtil.getCurrentUserId()));
    }

    @PostMapping("/records/{id}/pause")
    public ApiResponse<Void> pauseExam(@PathVariable Integer id,
                                        @RequestBody PauseExamDTO dto) {
        examService.pauseExam(id, dto, SecurityUtil.getCurrentUserId());
        return ApiResponse.ok(null, "已暂停");
    }

    @PostMapping("/records/{id}/resume")
    public ApiResponse<ResumeExamResponse> resumeExam(@PathVariable Integer id) {
        return ApiResponse.ok(examService.resumeExam(id, SecurityUtil.getCurrentUserId()));
    }

    @GetMapping("/records/{id}")
    public ApiResponse<ExamResultDTO> getRecordDetail(@PathVariable Integer id) {
        return ApiResponse.ok(examService.getRecordDetail(id, SecurityUtil.getCurrentUserId()));
    }

    @GetMapping("/{id}/questions")
    public ApiResponse<List<Question>> getExamQuestions(@PathVariable Integer id) {
        return ApiResponse.ok(examService.getExamQuestions(id, SecurityUtil.getCurrentUserId()));
    }

    /** AI 考试总结 */
    @GetMapping("/records/{id}/ai-summary")
    public ApiResponse<String> getAISummary(@PathVariable Integer id) {
        ExamResultDTO result = examService.getRecordDetail(id, SecurityUtil.getCurrentUserId());
        StringBuilder sb = new StringBuilder();
        sb.append("分数：").append(result.getScore()).append("/100\n");
        sb.append("正确：").append(result.getCorrectCount()).append("题 / 错误：").append(result.getWrongCount()).append("题 / 总计：").append(result.getTotalQuestions()).append("题\n");
        sb.append("用时：").append(result.getDurationUsed() != null ? result.getDurationUsed() / 60 + "分钟" : "未知").append("\n");
        if (result.getQuestions() != null) {
            sb.append("答错题目：\n");
            result.getQuestions().stream().filter(q -> !q.getIsCorrect()).forEach(q ->
                sb.append("- [").append(q.getType()).append("] ").append(truncate(q.getContent(), 50)).append(" (正确答案:").append(q.getCorrectAnswer()).append(" 你的答案:").append(q.getSelectedAnswer()).append(")\n"));
        }
        String system = "你是一位学习辅导专家。请根据考试成绩给出综合评价、薄弱环节分析和针对性建议（200字以内）。";
        return ApiResponse.ok(aiService.analyze(system, sb.toString()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteExam(@PathVariable Integer id) {
        examService.deleteExam(id, SecurityUtil.getCurrentUserId());
        return ApiResponse.ok(null, "删除成功");
    }

    private String truncate(String s, int max) {
        return s != null && s.length() > max ? s.substring(0, max) + "..." : s;
    }
}
