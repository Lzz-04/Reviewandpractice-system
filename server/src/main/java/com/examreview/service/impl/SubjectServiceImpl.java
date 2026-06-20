package com.examreview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.examreview.entity.Subject;
import com.examreview.exception.BusinessException;
import com.examreview.entity.Question;
import com.examreview.mapper.QuestionMapper;
import com.examreview.mapper.SubjectMapper;
import com.examreview.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 科目服务实现类
 * 实现科目管理的核心业务逻辑，支持用户数据隔离
 */
@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {

    private final SubjectMapper subjectMapper;
    private final QuestionMapper questionMapper;

    @Override
    public List<Subject> getAll(Long userId) {
        // 仅查看当前用户自己的科目
        return subjectMapper.selectList(
                new LambdaQueryWrapper<Subject>()
                        .eq(Subject::getUserId, userId)
                        .orderByAsc(Subject::getSortOrder));
    }

    @Override
    public Subject getById(Integer id, Long userId) {
        Subject subject = subjectMapper.selectOne(
                new LambdaQueryWrapper<Subject>()
                        .eq(Subject::getId, id)
                        .eq(Subject::getUserId, userId));
        if (subject == null) {
            throw new BusinessException("科目不存在");
        }
        return subject;
    }

    @Override
    public Subject create(Subject subject, Long userId) {
        // 校验科目名称非空
        if (subject.getName() == null || subject.getName().trim().isEmpty()) {
            throw new BusinessException("科目名称不能为空");
        }
        // 校验科目名称长度
        if (subject.getName().trim().length() > 50) {
            throw new BusinessException("科目名称不能超过50个字符");
        }
        // 检查同名科目（当前用户范围内）
        Long count = subjectMapper.selectCount(
                new LambdaQueryWrapper<Subject>()
                        .eq(Subject::getUserId, userId)
                        .eq(Subject::getName, subject.getName().trim()));
        if (count > 0) {
            throw new BusinessException("科目名称已存在");
        }
        subject.setName(subject.getName().trim());
        subject.setUserId(userId);
        subjectMapper.insert(subject);
        return subject;
    }

    @Override
    public Subject update(Integer id, Subject subject, Long userId) {
        // 校验科目存在且属于当前用户
        getById(id, userId);
        if (subject.getName() != null) {
            // 校验科目名称非空
            if (subject.getName().trim().isEmpty()) {
                throw new BusinessException("科目名称不能为空");
            }
            // 校验科目名称长度
            if (subject.getName().trim().length() > 50) {
                throw new BusinessException("科目名称不能超过50个字符");
            }
            // 同名检测排除自身
            Long count = subjectMapper.selectCount(
                    new LambdaQueryWrapper<Subject>()
                            .eq(Subject::getUserId, userId)
                            .eq(Subject::getName, subject.getName().trim())
                            .ne(Subject::getId, id));
            if (count > 0) {
                throw new BusinessException("科目名称已存在");
            }
            subject.setName(subject.getName().trim());
        }
        subject.setId(id);
        subjectMapper.updateById(subject);
        return subjectMapper.selectById(id);
    }

    @Override
    @Transactional
    public void delete(Integer id, Long userId) {
        // 校验科目存在且属于当前用户
        getById(id, userId);
        // 检查关联题目数，有题目则阻止删除
        Long questionCount = questionMapper.selectCount(
                new LambdaQueryWrapper<Question>()
                        .eq(Question::getSubjectId, id)
                        .eq(Question::getUserId, userId));
        if (questionCount > 0) {
            throw new BusinessException("该科目下还有 " + questionCount + " 道题目，请先删除题目");
        }
        subjectMapper.deleteById(id);
    }
}
