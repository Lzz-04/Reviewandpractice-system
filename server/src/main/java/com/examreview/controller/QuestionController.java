package com.examreview.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.examreview.dto.ApiResponse;
import com.examreview.entity.Question;
import com.examreview.service.QuestionService;
import com.examreview.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 题库管理控制器
 * 处理题目的增删改查、批量操作和随机抽题等功能
 */
@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    /**
     * 分页查询题目列表
     * 支持多条件组合筛选和排序
     *
     * @param page       页码（默认1）
     * @param pageSize   每页大小（默认20）
     * @param subjectId  科目ID（可选）
     * @param type       题型（single/multiple/judge，可选）
     * @param keyword    关键词（模糊匹配题目内容，可选）
     * @param difficulty 难度（1-5，可选）
     * @param sortBy     排序字段（difficulty或默认按创建时间降序）
     * @return 分页题目列表
     */
    @GetMapping
    public ApiResponse<Page<Question>> getList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Integer subjectId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) String sortBy) {
        return ApiResponse.ok(questionService.getList(page, pageSize, subjectId, type, keyword, difficulty, sortBy, SecurityUtil.getCurrentUserId()));
    }

    /**
     * 根据ID获取题目详情
     *
     * @param id 题目ID
     * @return 题目详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Question> getById(@PathVariable Integer id) {
        return ApiResponse.ok(questionService.getById(id, SecurityUtil.getCurrentUserId()));
    }

    /**
     * 创建题目
     * 校验题型必须为 single/multiple/judge，选项以JSON格式存储
     *
     * @param question 题目信息
     * @return 创建后的题目
     */
    @PostMapping
    public ApiResponse<Question> create(@RequestBody Question question) {
        return ApiResponse.ok(questionService.create(question, SecurityUtil.getCurrentUserId()), "创建成功");
    }

    /**
     * 更新题目
     * 支持部分字段更新
     *
     * @param id       题目ID
     * @param question 更新后的题目信息
     * @return 更新后的题目
     */
    @PutMapping("/{id}")
    public ApiResponse<Question> update(@PathVariable Integer id, @RequestBody Question question) {
        return ApiResponse.ok(questionService.update(id, question, SecurityUtil.getCurrentUserId()), "更新成功");
    }

    /**
     * 删除题目
     * 级联删除关联的错题记录、答题记录和试卷题目关联
     *
     * @param id 题目ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Integer id) {
        questionService.delete(id, SecurityUtil.getCurrentUserId());
        return ApiResponse.ok(null, "删除成功");
    }

    /**
     * 批量删除题目
     * 使用@Transactional保证原子性，任一失败全部回滚
     *
     * @param ids 题目ID列表
     * @return 操作结果
     */
    @DeleteMapping("/batch")
    public ApiResponse<Void> batchDelete(@RequestBody List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return ApiResponse.fail("题目ID列表不能为空");
        }
        questionService.batchDelete(ids, SecurityUtil.getCurrentUserId());
        return ApiResponse.ok(null, "批量删除成功");
    }

    /**
     * 批量更新题目
     * 支持批量修改难度
     *
     * @param body 请求体，包含ids（题目ID列表）、difficulty（难度，可选）
     * @return 操作结果
     */
    @PutMapping("/batch")
    public ApiResponse<Void> batchUpdate(@RequestBody Map<String, Object> body) {
        Long userId = SecurityUtil.getCurrentUserId();
        @SuppressWarnings("unchecked")
        List<Object> rawIds = (List<Object>) body.get("ids");
        if (rawIds == null || rawIds.isEmpty()) {
            return ApiResponse.fail("题目ID列表不能为空");
        }
        List<Integer> ids = rawIds.stream()
                .map(o -> o instanceof Number ? ((Number) o).intValue() : Integer.parseInt(o.toString()))
                .toList();
        Integer difficulty = body.get("difficulty") != null ? ((Number) body.get("difficulty")).intValue() : null;
        for (Integer id : ids) {
            Question q = questionService.getById(id, userId);
            if (difficulty != null) q.setDifficulty(difficulty);
            questionService.update(id, q, userId);
        }
        return ApiResponse.ok(null, "批量更新成功");
    }

    /**
     * 随机抽题
     * 从指定科目随机抽取指定数量的题目
     *
     * @param subjectId 科目ID
     * @param count     抽取数量（默认10）
     * @return 随机题目列表
     */
    @GetMapping("/random")
    public ApiResponse<?> getRandom(
            @RequestParam Integer subjectId,
            @RequestParam(defaultValue = "10") Integer count) {
        return ApiResponse.ok(questionService.getRandomQuestions(subjectId, count, SecurityUtil.getCurrentUserId()));
    }
}
