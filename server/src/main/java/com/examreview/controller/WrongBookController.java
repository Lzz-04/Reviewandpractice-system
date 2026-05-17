package com.examreview.controller;

import com.examreview.dto.ApiResponse;
import com.examreview.dto.WrongQuestionDTO;
import com.examreview.service.WrongBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wrongbook")
@RequiredArgsConstructor
public class WrongBookController {

    private final WrongBookService wrongBookService;

    @GetMapping
    public ApiResponse<List<WrongQuestionDTO>> getList(
            @RequestParam(required = false) Integer subjectId,
            @RequestParam(required = false) Integer mastered) {
        return ApiResponse.ok(wrongBookService.getList(subjectId, mastered));
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getStats() {
        return ApiResponse.ok(wrongBookService.getStats());
    }

    @PostMapping("/{id}/review")
    public ApiResponse<Void> review(@PathVariable Integer id) {
        wrongBookService.review(id);
        return ApiResponse.ok(null, "复习标记成功");
    }

    @PostMapping("/{id}/master")
    public ApiResponse<Void> master(@PathVariable Integer id) {
        wrongBookService.master(id);
        return ApiResponse.ok(null, "操作成功");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> remove(@PathVariable Integer id) {
        wrongBookService.remove(id);
        return ApiResponse.ok(null, "已从错题本移除");
    }
}
