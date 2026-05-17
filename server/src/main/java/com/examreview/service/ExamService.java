package com.examreview.service;

import com.examreview.dto.ExamGenerateDTO;
import com.examreview.dto.ExamResultDTO;
import com.examreview.dto.ExamSubmitDTO;
import com.examreview.entity.ExamPaper;
import com.examreview.entity.ExamRecord;

import java.util.List;

public interface ExamService {
    List<ExamPaper> getList(Integer subjectId);
    ExamPaper getById(Integer id);
    ExamPaper create(ExamPaper examPaper);
    ExamPaper generate(ExamGenerateDTO dto);
    ExamRecord startExam(Integer examId);
    ExamResultDTO submitExam(ExamSubmitDTO dto);
    List<ExamRecord> getRecords();
    ExamResultDTO getRecordDetail(Integer recordId);
    void deleteExam(Integer id);
}
