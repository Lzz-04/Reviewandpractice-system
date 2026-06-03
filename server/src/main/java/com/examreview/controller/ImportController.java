package com.examreview.controller;

import com.examreview.dto.ApiResponse;
import com.examreview.dto.ImportResultDTO;
import com.examreview.service.ImportService;
import com.examreview.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
public class ImportController {

    private final ImportService importService;

    @PostMapping
    public ApiResponse<ImportResultDTO> importQuestions(
            @RequestParam("file") MultipartFile file,
            @RequestParam Integer subjectId,
            @RequestParam Integer chapterId) throws IOException {
        if (file.isEmpty()) {
            return ApiResponse.fail("文件不能为空");
        }
        ImportResultDTO result = importService.importQuestions(file, subjectId, chapterId, SecurityUtil.getCurrentUserId());
        String msg = "导入完成：成功 " + result.getSuccess() + " 题";
        if (result.getFailed() > 0) {
            msg += "，失败 " + result.getFailed() + " 题";
        }
        return ApiResponse.ok(result, msg);
    }

    @PostMapping("/preview")
    public ApiResponse<String> previewExtract(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ApiResponse.fail("文件不能为空");
        }
        String preview = importService.previewExtract(file);
        return ApiResponse.ok(preview);
    }
}
