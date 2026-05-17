package com.examreview.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.examreview.entity.Question;
import java.util.List;

public interface QuestionService {
    Page<Question> getList(Integer page, Integer pageSize, Integer subjectId, Integer chapterId, String type);
    Question getById(Integer id);
    Question create(Question question);
    Question update(Integer id, Question question);
    void delete(Integer id);
    List<Question> getRandomQuestions(Integer chapterId, Integer count);
    List<Question> getQuestionsByIds(List<Integer> ids);
}
