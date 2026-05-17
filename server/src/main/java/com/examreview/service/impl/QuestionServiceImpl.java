package com.examreview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.examreview.entity.AnswerRecord;
import com.examreview.entity.ExamQuestion;
import com.examreview.entity.Question;
import com.examreview.entity.WrongQuestion;
import com.examreview.exception.BusinessException;
import com.examreview.mapper.AnswerRecordMapper;
import com.examreview.mapper.ExamQuestionMapper;
import com.examreview.mapper.QuestionMapper;
import com.examreview.mapper.WrongQuestionMapper;
import com.examreview.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionMapper questionMapper;
    private final WrongQuestionMapper wrongQuestionMapper;
    private final AnswerRecordMapper answerRecordMapper;
    private final ExamQuestionMapper examQuestionMapper;

    @Override
    public Page<Question> getList(Integer page, Integer pageSize, Integer subjectId, Integer chapterId, String type,
                                   String keyword, Integer difficulty, String sortBy) {
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 20;
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        if (subjectId != null) {
            wrapper.eq(Question::getSubjectId, subjectId);
        }
        if (chapterId != null) {
            wrapper.eq(Question::getChapterId, chapterId);
        }
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Question::getType, type);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Question::getContent, keyword.trim());
        }
        if (difficulty != null) {
            wrapper.eq(Question::getDifficulty, difficulty);
        }
        if ("difficulty".equals(sortBy)) {
            wrapper.orderByAsc(Question::getDifficulty).orderByDesc(Question::getCreatedAt);
        } else {
            wrapper.orderByDesc(Question::getCreatedAt);
        }
        return questionMapper.selectPage(new Page<>(page, pageSize), wrapper);
    }

    @Override
    public Question getById(Integer id) {
        Question question = questionMapper.selectById(id);
        if (question == null) {
            throw new BusinessException("题目不存在");
        }
        return question;
    }

    @Override
    public Question create(Question question) {
        validateQuestion(question);
        if (question.getOptions() == null || question.getOptions().isEmpty()) {
            question.setOptions("[]");
        }
        questionMapper.insert(question);
        return question;
    }

    private void validateQuestion(Question question) {
        if (question.getChapterId() == null) {
            throw new BusinessException("所属章节不能为空");
        }
        if (question.getSubjectId() == null) {
            throw new BusinessException("所属科目不能为空");
        }
        if (question.getType() == null || question.getType().trim().isEmpty()) {
            throw new BusinessException("题型不能为空");
        }
        List<String> validTypes = Arrays.asList("single", "multiple", "judge");
        if (!validTypes.contains(question.getType())) {
            throw new BusinessException("无效题型：" + question.getType() + "，有效值为 single/multiple/judge");
        }
        if (question.getContent() == null || question.getContent().trim().isEmpty()) {
            throw new BusinessException("题目内容不能为空");
        }
        if (question.getAnswer() == null || question.getAnswer().trim().isEmpty()) {
            throw new BusinessException("答案不能为空");
        }
    }

    @Override
    public Question update(Integer id, Question question) {
        getById(id);
        question.setId(id);
        questionMapper.updateById(question);
        return questionMapper.selectById(id);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        getById(id); // 确保题目存在
        // 依次删除关联记录，解除外键约束
        wrongQuestionMapper.delete(new LambdaQueryWrapper<WrongQuestion>().eq(WrongQuestion::getQuestionId, id));
        answerRecordMapper.delete(new LambdaQueryWrapper<AnswerRecord>().eq(AnswerRecord::getQuestionId, id));
        examQuestionMapper.delete(new LambdaQueryWrapper<ExamQuestion>().eq(ExamQuestion::getQuestionId, id));
        questionMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void batchDelete(List<Integer> ids) {
        for (Integer id : ids) {
            delete(id);
        }
    }

    @Override
    public List<Question> getRandomQuestions(Integer chapterId, Integer count) {
        List<Question> all = questionMapper.selectByChapter(chapterId);
        Collections.shuffle(all);
        return all.subList(0, Math.min(count, all.size()));
    }

    @Override
    public List<Question> getQuestionsByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return questionMapper.selectBatchIds(ids);
    }
}
