package com.examreview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.examreview.entity.Chapter;
import com.examreview.entity.Subject;
import com.examreview.exception.BusinessException;
import com.examreview.mapper.ChapterMapper;
import com.examreview.mapper.SubjectMapper;
import com.examreview.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {

    private final SubjectMapper subjectMapper;
    private final ChapterMapper chapterMapper;

    @Override
    public List<Subject> getAll() {
        return subjectMapper.selectList(
                new LambdaQueryWrapper<Subject>().orderByAsc(Subject::getSortOrder));
    }

    @Override
    public Subject getById(Integer id) {
        Subject subject = subjectMapper.selectById(id);
        if (subject == null) {
            throw new BusinessException("科目不存在");
        }
        return subject;
    }

    @Override
    public Subject create(Subject subject) {
        if (subject.getName() == null || subject.getName().trim().isEmpty()) {
            throw new BusinessException("科目名称不能为空");
        }
        if (subject.getName().trim().length() > 50) {
            throw new BusinessException("科目名称不能超过50个字符");
        }
        // 检查同名科目
        Long count = subjectMapper.selectCount(
                new LambdaQueryWrapper<Subject>().eq(Subject::getName, subject.getName().trim()));
        if (count > 0) {
            throw new BusinessException("科目名称已存在");
        }
        subject.setName(subject.getName().trim());
        subjectMapper.insert(subject);
        return subject;
    }

    @Override
    public Subject update(Integer id, Subject subject) {
        getById(id); // 校验科目存在
        if (subject.getName() != null) {
            if (subject.getName().trim().isEmpty()) {
                throw new BusinessException("科目名称不能为空");
            }
            if (subject.getName().trim().length() > 50) {
                throw new BusinessException("科目名称不能超过50个字符");
            }
            // 检查同名科目（排除自身）
            Long count = subjectMapper.selectCount(
                    new LambdaQueryWrapper<Subject>()
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
    public void delete(Integer id) {
        getById(id);
        // 检查是否存在关联章节
        Long chapterCount = chapterMapper.selectCount(
                new LambdaQueryWrapper<Chapter>().eq(Chapter::getSubjectId, id));
        if (chapterCount > 0) {
            throw new BusinessException("该科目下还有 " + chapterCount + " 个章节，请先删除章节");
        }
        subjectMapper.deleteById(id);
    }
}
