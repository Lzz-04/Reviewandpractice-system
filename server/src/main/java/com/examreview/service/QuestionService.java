package com.examreview.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.examreview.entity.Question;
import java.util.List;

public interface QuestionService {
    Page<Question> getList(Integer page, Integer pageSize, Integer subjectId, Integer chapterId, String type,
                         String keyword, Integer difficulty, String sortBy, Long userId);
    Question getById(Integer id, Long userId);
    Question create(Question question, Long userId);
    Question update(Integer id, Question question, Long userId);
    void delete(Integer id, Long userId);
    void batchDelete(List<Integer> ids, Long userId);
    List<Question> getRandomQuestions(Integer chapterId, Integer count, Long userId);
    List<Question> getQuestionsByIds(List<Integer> ids, Long userId);
}
