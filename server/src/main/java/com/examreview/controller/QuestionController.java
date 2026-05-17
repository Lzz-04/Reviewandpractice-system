package com.examreview.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.examreview.dto.ApiResponse;
import com.examreview.entity.Question;
import com.examreview.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping
    public ApiResponse<Page<Question>> getList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Integer subjectId,
            @RequestParam(required = false) Integer chapterId,
            @RequestParam(required = false) String type) {
        return ApiResponse.ok(questionService.getList(page, pageSize, subjectId, chapterId, type));
    }

    @GetMapping("/{id}")
    public ApiResponse<Question> getById(@PathVariable Integer id) {
        return ApiResponse.ok(questionService.getById(id));
    }

    @PostMapping
    public ApiResponse<Question> create(@RequestBody Question question) {
        return ApiResponse.ok(questionService.create(question), "创建成功");
    }

    @PutMapping("/{id}")
    public ApiResponse<Question> update(@PathVariable Integer id, @RequestBody Question question) {
        return ApiResponse.ok(questionService.update(id, question), "更新成功");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Integer id) {
        questionService.delete(id);
        return ApiResponse.ok(null, "删除成功");
    }

    @GetMapping("/random")
    public ApiResponse<?> getRandom(
            @RequestParam Integer chapterId,
            @RequestParam(defaultValue = "10") Integer count) {
        return ApiResponse.ok(questionService.getRandomQuestions(chapterId, count));
    }
}
