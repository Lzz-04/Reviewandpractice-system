package com.examreview.controller;

import com.examreview.dto.ApiResponse;
import com.examreview.dto.ImportResultDTO;
import com.examreview.service.ImportService;
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
        ImportResultDTO result = importService.importQuestions(file, subjectId, chapterId);
        String msg = "导入完成：成功 " + result.getSuccess() + " 题";
        if (result.getFailed() > 0) {
            msg += "，失败 " + result.getFailed() + " 题";
        }
        return ApiResponse.ok(result, msg);
    }
}
