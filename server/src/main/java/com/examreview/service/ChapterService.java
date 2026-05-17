package com.examreview.service;

import com.examreview.entity.Chapter;
import java.util.List;

public interface ChapterService {
    List<Chapter> getBySubjectId(Integer subjectId);
    Chapter create(Chapter chapter);
    Chapter update(Integer id, Chapter chapter);
    void delete(Integer id);
}
