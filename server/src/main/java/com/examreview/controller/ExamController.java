package com.examreview.controller;

import com.examreview.dto.*;
import com.examreview.entity.ExamPaper;
import com.examreview.entity.ExamRecord;
import com.examreview.service.ExamService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    @GetMapping
    public ApiResponse<List<ExamPaper>> getList(@RequestParam(required = false) Integer subjectId) {
        return ApiResponse.ok(examService.getList(subjectId));
    }

    @GetMapping("/{id}")
    public ApiResponse<ExamPaper> getById(@PathVariable Integer id) {
        return ApiResponse.ok(examService.getById(id));
    }

    @PostMapping
    public ApiResponse<ExamPaper> create(@RequestBody ExamPaper examPaper) {
        return ApiResponse.ok(examService.create(examPaper), "创建成功");
    }

    @PostMapping("/generate")
    public ApiResponse<ExamPaper> generate(@RequestBody @Valid ExamGenerateDTO dto) {
        return ApiResponse.ok(examService.generate(dto), "组卷成功");
    }

    @PostMapping("/{id}/start")
    public ApiResponse<ExamRecord> startExam(@PathVariable Integer id) {
        return ApiResponse.ok(examService.startExam(id));
    }

    @PostMapping("/submit")
    public ApiResponse<ExamResultDTO> submitExam(@RequestBody @Valid ExamSubmitDTO dto) {
        return ApiResponse.ok(examService.submitExam(dto), "交卷成功");
    }

    @GetMapping("/records")
    public ApiResponse<List<ExamRecord>> getRecords() {
        return ApiResponse.ok(examService.getRecords());
    }

    @GetMapping("/records/{id}")
    public ApiResponse<ExamResultDTO> getRecordDetail(@PathVariable Integer id) {
        return ApiResponse.ok(examService.getRecordDetail(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteExam(@PathVariable Integer id) {
        examService.deleteExam(id);
        return ApiResponse.ok(null, "删除成功");
    }
}
