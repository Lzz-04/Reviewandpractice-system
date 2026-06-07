package com.examreview.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.examreview.dto.*;
import com.examreview.entity.ExamPaper;
import com.examreview.entity.ExamRecord;

import java.util.List;

public interface ExamService {
    Page<ExamPaper> getList(Integer page, Integer pageSize, Integer subjectId, Long userId);
    ExamPaper getById(Integer id, Long userId);
    ExamPaper create(ExamPaper examPaper, Long userId);
    ExamPaper generate(ExamGenerateDTO dto, Long userId);
    ExamRecord startExam(Integer examId, Long userId);
    ExamResultDTO submitExam(ExamSubmitDTO dto, Long userId);
    void pauseExam(Integer recordId, PauseExamDTO dto, Long userId);
    ResumeExamResponse resumeExam(Integer recordId, Long userId);
    Page<ExamRecord> getRecords(Integer page, Integer pageSize, Long userId);
    ExamResultDTO getRecordDetail(Integer recordId, Long userId);
    void deleteExam(Integer id, Long userId);
    List<com.examreview.entity.Question> getExamQuestions(Integer examId, Long userId);
    /** 获取当前用户活跃的考试记录（in_progress + paused） */
    List<ExamRecord> getActiveRecords(Long userId);
}
