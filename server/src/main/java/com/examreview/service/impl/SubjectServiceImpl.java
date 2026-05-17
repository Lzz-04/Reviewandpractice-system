package com.examreview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.examreview.entity.Subject;
import com.examreview.exception.BusinessException;
import com.examreview.mapper.SubjectMapper;
import com.examreview.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {

    private final SubjectMapper subjectMapper;

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
        subjectMapper.insert(subject);
        return subject;
    }

    @Override
    public Subject update(Integer id, Subject subject) {
        Subject existing = getById(id);
        subject.setId(id);
        subjectMapper.updateById(subject);
        return subjectMapper.selectById(id);
    }

    @Override
    public void delete(Integer id) {
        getById(id);
        subjectMapper.deleteById(id);
    }
}
