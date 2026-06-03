package com.examreview.controller;

import com.examreview.dto.ApiResponse;
import com.examreview.entity.Chapter;
import com.examreview.service.ChapterService;
import com.examreview.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChapterController {

    private final ChapterService chapterService;

    @GetMapping("/subjects/{subjectId}/chapters")
    public ApiResponse<List<Chapter>> getBySubject(@PathVariable Integer subjectId) {
        return ApiResponse.ok(chapterService.getBySubjectId(subjectId, SecurityUtil.getCurrentUserId()));
    }

    @PostMapping("/subjects/{subjectId}/chapters")
    public ApiResponse<Chapter> create(@PathVariable Integer subjectId, @RequestBody Chapter chapter) {
        chapter.setSubjectId(subjectId);
        return ApiResponse.ok(chapterService.create(chapter, SecurityUtil.getCurrentUserId()), "创建成功");
    }

    @PutMapping("/chapters/{id}")
    public ApiResponse<Chapter> update(@PathVariable Integer id, @RequestBody Chapter chapter) {
        return ApiResponse.ok(chapterService.update(id, chapter, SecurityUtil.getCurrentUserId()), "更新成功");
    }

    @DeleteMapping("/chapters/{id}")
    public ApiResponse<Void> delete(@PathVariable Integer id) {
        chapterService.delete(id, SecurityUtil.getCurrentUserId());
        return ApiResponse.ok(null, "删除成功");
    }
}
