package com.examreview.service;

import com.examreview.entity.Subject;
import java.util.List;

public interface SubjectService {
    List<Subject> getAll(Long userId);
    Subject getById(Integer id, Long userId);
    Subject create(Subject subject, Long userId);
    Subject update(Integer id, Subject subject, Long userId);
    void delete(Integer id, Long userId);
}
