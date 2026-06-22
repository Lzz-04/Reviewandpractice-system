package com.examreview.controller;

import com.examreview.dto.ApiResponse;
import com.examreview.dto.ImportResultDTO;
import com.examreview.service.ImportService;
import com.examreview.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 文件导入控制器
 * 处理题目批量导入和预览功能，支持 XLSX/DOCX/TXT 四种格式
 */
@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
public class ImportController {

    private final ImportService importService;

    /**
     * 导入题目
     * 解析上传文件，批量导入题目到指定科目
     *
     * @param file      上传的文件（XLSX/DOCX/TXT）
     * @param subjectId 科目ID
     * @return 导入结果（总数/成功数/失败数/错误详情）
     */
    @PostMapping
    public ApiResponse<ImportResultDTO> importQuestions(
            @RequestParam("file") MultipartFile file,
            @RequestParam Integer subjectId) throws IOException {
        if (file.isEmpty()) {
            return ApiResponse.fail("文件不能为空");
        }
        ImportResultDTO result = importService.importQuestions(file, subjectId, SecurityUtil.getCurrentUserId());
        String msg = "导入完成：成功 " + result.getSuccess() + " 题";
        if (result.getFailed() > 0) {
            msg += "，失败 " + result.getFailed() + " 题";
        }
        return ApiResponse.ok(result, msg);
    }

    /**
     * 预览提取结果
     * 仅解析文件提取前 10 道单选题，不写入数据库
     *
     * @param file 上传的文件
     * @return 预览文本（标准格式）
     */
    @PostMapping("/preview")
    public ApiResponse<String> previewExtract(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ApiResponse.fail("文件不能为空");
        }
        String preview = importService.previewExtract(file);
        return ApiResponse.ok(preview);
    }
}
