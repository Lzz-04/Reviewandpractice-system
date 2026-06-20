package com.examreview.controller;

import com.examreview.dto.ApiResponse;
import com.examreview.entity.Subject;
import com.examreview.service.SubjectService;
import com.examreview.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 科目管理控制器
 * 处理科目的增删改查操作，所有操作均按当前用户隔离数据
 */
@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    /**
     * 获取当前用户的所有科目列表
     * 按 sort_order 升序排列
     *
     * @return 科目列表
     */
    @GetMapping
    public ApiResponse<List<Subject>> getAll() {
        return ApiResponse.ok(subjectService.getAll(SecurityUtil.getCurrentUserId()));
    }

    /**
     * 根据ID获取科目详情
     * 校验科目是否属于当前用户
     *
     * @param id 科目ID
     * @return 科目详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Subject> getById(@PathVariable Integer id) {
        return ApiResponse.ok(subjectService.getById(id, SecurityUtil.getCurrentUserId()));
    }

    /**
     * 创建科目
     * 校验科目名称非空、≤50字符、不重名
     *
     * @param subject 科目信息
     * @return 创建后的科目
     */
    @PostMapping
    public ApiResponse<Subject> create(@RequestBody Subject subject) {
        return ApiResponse.ok(subjectService.create(subject, SecurityUtil.getCurrentUserId()), "创建成功");
    }

    /**
     * 更新科目
     * 同名检测时排除自身
     *
     * @param id      科目ID
     * @param subject 更新后的科目信息
     * @return 更新后的科目
     */
    @PutMapping("/{id}")
    public ApiResponse<Subject> update(@PathVariable Integer id, @RequestBody Subject subject) {
        return ApiResponse.ok(subjectService.update(id, subject, SecurityUtil.getCurrentUserId()), "更新成功");
    }

    /**
     * 删除科目
     * 检查关联章节数，>0则阻止删除
     *
     * @param id 科目ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Integer id) {
        subjectService.delete(id, SecurityUtil.getCurrentUserId());
        return ApiResponse.ok(null, "删除成功");
    }
}
