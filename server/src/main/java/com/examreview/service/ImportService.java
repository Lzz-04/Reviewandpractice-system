package com.examreview.service;

import com.examreview.dto.ImportResultDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 文件导入服务接口
 * 提供题目批量导入和预览提取功能
 */
public interface ImportService {

    /**
     * 导入题目
     * 解析上传文件，批量导入题目到指定科目
     *
     * @param file      上传的文件（XLSX/DOCX/TXT）
     * @param subjectId 科目 ID
     * @param userId    用户 ID
     * @return 导入结果（总数/成功数/失败数/错误详情）
     */
    ImportResultDTO importQuestions(MultipartFile file, Integer subjectId, Long userId) throws IOException;

    /**
     * 预览提取
     * 解析文件，提取前 10 道单选题并格式化为标准文本，不写入数据库
     *
     * @param file 上传的文件
     * @return 预览文本
     */
    String previewExtract(MultipartFile file) throws IOException;
}
