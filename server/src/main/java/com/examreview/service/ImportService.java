package com.examreview.service;

import com.examreview.dto.ImportResultDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ImportService {
    ImportResultDTO importQuestions(MultipartFile file, Integer subjectId, Integer chapterId, Long userId) throws IOException;

    /**
     * 预览提取：解析文件，提取前10道单选题并格式化为标准文本
     */
    String previewExtract(MultipartFile file) throws IOException;
}
