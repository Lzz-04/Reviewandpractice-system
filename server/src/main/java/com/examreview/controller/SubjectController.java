package com.examreview.controller;

import com.examreview.dto.ApiResponse;
import com.examreview.entity.Subject;
import com.examreview.service.SubjectService;
import com.examreview.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    @GetMapping
    public ApiResponse<List<Subject>> getAll() {
        return ApiResponse.ok(subjectService.getAll(SecurityUtil.getCurrentUserId()));
    }

    @GetMapping("/{id}")
    public ApiResponse<Subject> getById(@PathVariable Integer id) {
        return ApiResponse.ok(subjectService.getById(id, SecurityUtil.getCurrentUserId()));
    }

    @PostMapping
    public ApiResponse<Subject> create(@RequestBody Subject subject) {
        return ApiResponse.ok(subjectService.create(subject, SecurityUtil.getCurrentUserId()), "创建成功");
    }

    @PutMapping("/{id}")
    public ApiResponse<Subject> update(@PathVariable Integer id, @RequestBody Subject subject) {
        return ApiResponse.ok(subjectService.update(id, subject, SecurityUtil.getCurrentUserId()), "更新成功");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Integer id) {
        subjectService.delete(id, SecurityUtil.getCurrentUserId());
        return ApiResponse.ok(null, "删除成功");
    }
}
