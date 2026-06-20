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

/**
 * 题库服务实现类
 * 实现题目管理的核心业务逻辑，支持分页查询、CRUD、批量操作和随机抽题
 */
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionMapper questionMapper;
    private final WrongQuestionMapper wrongQuestionMapper;
    private final AnswerRecordMapper answerRecordMapper;
    private final ExamQuestionMapper examQuestionMapper;

    @Override
    public Page<Question> getList(Integer page, Integer pageSize, Integer subjectId, String type,
                                   String keyword, Integer difficulty, String sortBy, Long userId) {
        // 参数校验
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 20;
        
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        // 数据隔离：仅查看当前用户自己的题目
        wrapper.eq(Question::getUserId, userId);
        
        // 条件拼接，空值自动跳过
        if (subjectId != null) {
            wrapper.eq(Question::getSubjectId, subjectId);
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
        
        // 排序：按难度升序时，再按创建时间降序；默认按创建时间降序
        if ("difficulty".equals(sortBy)) {
            wrapper.orderByAsc(Question::getDifficulty).orderByDesc(Question::getCreatedAt);
        } else {
            wrapper.orderByDesc(Question::getCreatedAt);
        }
        
        return questionMapper.selectPage(new Page<>(page, pageSize), wrapper);
    }

    @Override
    public Question getById(Integer id, Long userId) {
        Question question = questionMapper.selectOne(
                new LambdaQueryWrapper<Question>()
                        .eq(Question::getId, id)
                        .eq(Question::getUserId, userId));
        if (question == null) {
            throw new BusinessException("题目不存在");
        }
        return question;
    }

    @Override
    public Question create(Question question, Long userId) {
        // 校验题目信息
        validateQuestion(question);
        // 选项为空时设置默认值
        if (question.getOptions() == null || question.getOptions().isEmpty()) {
            question.setOptions("[]");
        }
        question.setUserId(userId);
        questionMapper.insert(question);
        return question;
    }

    /**
     * 校验题目信息
     * 校验字段：科目ID、题型、题目内容、答案
     */
    private void validateQuestion(Question question) {
        if (question.getSubjectId() == null) {
            throw new BusinessException("所属科目不能为空");
        }
        if (question.getType() == null || question.getType().trim().isEmpty()) {
            throw new BusinessException("题型不能为空");
        }
        // 校验题型有效值
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
    public Question update(Integer id, Question question, Long userId) {
        // 校验题目存在且属于当前用户
        getById(id, userId);
        question.setId(id);
        questionMapper.updateById(question);
        return questionMapper.selectById(id);
    }

    @Override
    @Transactional
    public void delete(Integer id, Long userId) {
        // 校验题目存在且属于当前用户
        getById(id, userId);
        
        // 级联删除关联数据
        // 删除错题记录
        wrongQuestionMapper.delete(new LambdaQueryWrapper<WrongQuestion>().eq(WrongQuestion::getQuestionId, id));
        // 删除答题记录
        answerRecordMapper.delete(new LambdaQueryWrapper<AnswerRecord>().eq(AnswerRecord::getQuestionId, id));
        // 删除试卷题目关联
        examQuestionMapper.delete(new LambdaQueryWrapper<ExamQuestion>().eq(ExamQuestion::getQuestionId, id));
        // 删除题目
        questionMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void batchDelete(List<Integer> ids, Long userId) {
        // 遍历删除，@Transactional 保证原子性
        for (Integer id : ids) {
            delete(id, userId);
        }
    }

    @Override
    public List<Question> getRandomQuestions(Integer subjectId, Integer count, Long userId) {
        // 查询指定科目的所有题目
        List<Question> all = questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                        .eq(Question::getSubjectId, subjectId)
                        .eq(Question::getUserId, userId));
        // 随机打乱
        Collections.shuffle(all);
        // 返回前 count 条
        return all.subList(0, Math.min(count, all.size()));
    }

    @Override
    public List<Question> getQuestionsByIds(List<Integer> ids, Long userId) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                        .in(Question::getId, ids)
                        .eq(Question::getUserId, userId));
    }
}
