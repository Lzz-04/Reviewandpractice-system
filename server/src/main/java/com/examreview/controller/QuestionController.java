package com.examreview.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.examreview.dto.ApiResponse;
import com.examreview.entity.Question;
import com.examreview.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


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
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) String sortBy) {
        return ApiResponse.ok(questionService.getList(page, pageSize, subjectId, chapterId, type, keyword, difficulty, sortBy));
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

    @DeleteMapping("/batch")
    public ApiResponse<Void> batchDelete(@RequestBody List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return ApiResponse.fail("题目ID列表不能为空");
        }
        for (Integer id : ids) {
            questionService.delete(id);
        }
        return ApiResponse.ok(null, "批量删除成功");
    }

    @PutMapping("/batch")
    public ApiResponse<Void> batchUpdate(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> ids = (List<Integer>) body.get("ids");
        if (ids == null || ids.isEmpty()) {
            return ApiResponse.fail("题目ID列表不能为空");
        }
        Integer chapterId = body.get("chapterId") != null ? ((Number) body.get("chapterId")).intValue() : null;
        Integer difficulty = body.get("difficulty") != null ? ((Number) body.get("difficulty")).intValue() : null;
        for (Integer id : ids) {
            Question q = questionService.getById(id);
            if (chapterId != null) q.setChapterId(chapterId);
            if (difficulty != null) q.setDifficulty(difficulty);
            questionService.update(id, q);
        }
        return ApiResponse.ok(null, "批量更新成功");
    }

    @GetMapping("/random")
    public ApiResponse<?> getRandom(
            @RequestParam Integer chapterId,
            @RequestParam(defaultValue = "10") Integer count) {
        return ApiResponse.ok(questionService.getRandomQuestions(chapterId, count));
    }
}
