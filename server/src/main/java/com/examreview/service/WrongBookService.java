package com.examreview.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.examreview.dto.WrongQuestionDTO;
import com.examreview.entity.Question;

import java.util.Map;

public interface WrongBookService {
    Page<WrongQuestionDTO> getList(Integer page, Integer pageSize, Integer subjectId, Integer mastered, Long userId);
    Map<String, Object> getStats(Long userId);
    void review(Integer id, Long userId);
    void master(Integer id, Long userId);
    void remove(Integer id, Long userId);
    void upsertWrongQuestion(Question question, Long userId);
}
