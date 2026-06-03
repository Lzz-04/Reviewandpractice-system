package com.examreview.controller;

import com.examreview.dto.ApiResponse;
import com.examreview.dto.WrongQuestionDTO;
import com.examreview.service.WrongBookService;
import com.examreview.util.SecurityUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/wrongbook")
@RequiredArgsConstructor
public class WrongBookController {

    private final WrongBookService wrongBookService;

    @GetMapping
    public ApiResponse<Page<WrongQuestionDTO>> getList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Integer subjectId,
            @RequestParam(required = false) Integer mastered) {
        return ApiResponse.ok(wrongBookService.getList(page, pageSize, subjectId, mastered, SecurityUtil.getCurrentUserId()));
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getStats() {
        return ApiResponse.ok(wrongBookService.getStats(SecurityUtil.getCurrentUserId()));
    }

    @PostMapping("/{id}/review")
    public ApiResponse<Void> review(@PathVariable Integer id) {
        wrongBookService.review(id, SecurityUtil.getCurrentUserId());
        return ApiResponse.ok(null, "复习标记成功");
    }

    @PostMapping("/{id}/master")
    public ApiResponse<Void> master(@PathVariable Integer id) {
        wrongBookService.master(id, SecurityUtil.getCurrentUserId());
        return ApiResponse.ok(null, "操作成功");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> remove(@PathVariable Integer id) {
        wrongBookService.remove(id, SecurityUtil.getCurrentUserId());
        return ApiResponse.ok(null, "已从错题本移除");
    }
}
