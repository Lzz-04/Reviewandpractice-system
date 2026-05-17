package com.examreview.service;

import com.examreview.dto.WrongQuestionDTO;
import com.examreview.entity.Question;

import java.util.List;
import java.util.Map;

public interface WrongBookService {
    List<WrongQuestionDTO> getList(Integer subjectId, Integer mastered);
    Map<String, Object> getStats();
    void review(Integer id);
    void master(Integer id);
    void remove(Integer id);
    void upsertWrongQuestion(Question question);
}
