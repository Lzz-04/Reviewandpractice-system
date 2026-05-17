package com.examreview.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.examreview.dto.WrongQuestionDTO;
import com.examreview.entity.Question;

import java.util.Map;

public interface WrongBookService {
    Page<WrongQuestionDTO> getList(Integer page, Integer pageSize, Integer subjectId, Integer mastered);
    Map<String, Object> getStats();
    void review(Integer id);
    void master(Integer id);
    void remove(Integer id);
    void upsertWrongQuestion(Question question);
}
