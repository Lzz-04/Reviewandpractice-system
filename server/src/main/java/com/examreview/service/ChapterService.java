package com.examreview.service;

import com.examreview.entity.Chapter;
import java.util.List;

public interface ChapterService {
    List<Chapter> getBySubjectId(Integer subjectId, Long userId);
    Chapter create(Chapter chapter, Long userId);
    Chapter update(Integer id, Chapter chapter, Long userId);
    void delete(Integer id, Long userId);
}
