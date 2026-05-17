package com.examreview.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.examreview.dto.ExamGenerateDTO;
import com.examreview.dto.ExamResultDTO;
import com.examreview.dto.ExamSubmitDTO;
import com.examreview.entity.ExamPaper;
import com.examreview.entity.ExamRecord;

import java.util.List;

public interface ExamService {
    Page<ExamPaper> getList(Integer page, Integer pageSize, Integer subjectId);
    ExamPaper getById(Integer id);
    ExamPaper create(ExamPaper examPaper);
    ExamPaper generate(ExamGenerateDTO dto);
    ExamRecord startExam(Integer examId);
    ExamResultDTO submitExam(ExamSubmitDTO dto);
    void pauseExam(Integer recordId, Integer remainingSeconds);
    ExamRecord resumeExam(Integer recordId);
    Page<ExamRecord> getRecords(Integer page, Integer pageSize);
    ExamResultDTO getRecordDetail(Integer recordId);
    void deleteExam(Integer id);
    List<com.examreview.entity.Question> getExamQuestions(Integer examId);
}
