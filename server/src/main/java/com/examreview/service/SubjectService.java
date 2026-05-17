package com.examreview.service;

import com.examreview.entity.Subject;
import java.util.List;

public interface SubjectService {
    List<Subject> getAll();
    Subject getById(Integer id);
    Subject create(Subject subject);
    Subject update(Integer id, Subject subject);
    void delete(Integer id);
}
