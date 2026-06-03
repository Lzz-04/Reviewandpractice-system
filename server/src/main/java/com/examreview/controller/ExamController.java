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

import com.examreview.entity.Question;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

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

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteExam(@PathVariable Integer id) {
        examService.deleteExam(id, SecurityUtil.getCurrentUserId());
        return ApiResponse.ok(null, "删除成功");
    }
}
