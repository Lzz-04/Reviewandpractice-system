package com.examreview.service;

import com.examreview.dto.ImportResultDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ImportService {
    ImportResultDTO importQuestions(MultipartFile file, Integer subjectId, Integer chapterId) throws IOException;
}
