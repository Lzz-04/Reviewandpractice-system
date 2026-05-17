package com.examreview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.examreview.entity.Chapter;
import com.examreview.entity.Question;
import com.examreview.entity.WrongQuestion;
import com.examreview.exception.BusinessException;
import com.examreview.mapper.ChapterMapper;
import com.examreview.mapper.QuestionMapper;
import com.examreview.mapper.WrongQuestionMapper;
import com.examreview.service.ChapterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChapterServiceImpl implements ChapterService {

    private final ChapterMapper chapterMapper;
    private final QuestionMapper questionMapper;
    private final WrongQuestionMapper wrongQuestionMapper;

    @Override
    public List<Chapter> getBySubjectId(Integer subjectId) {
        return chapterMapper.selectList(
                new LambdaQueryWrapper<Chapter>()
                        .eq(Chapter::getSubjectId, subjectId)
                        .orderByAsc(Chapter::getSortOrder));
    }

    @Override
    public Chapter create(Chapter chapter) {
        if (chapter.getName() == null || chapter.getName().trim().isEmpty()) {
            throw new BusinessException("章节名称不能为空");
        }
        // 自动分配 sort_order：当前科目下最大值 + 1
        List<Chapter> existing = chapterMapper.selectList(
                new LambdaQueryWrapper<Chapter>()
                        .eq(Chapter::getSubjectId, chapter.getSubjectId())
                        .orderByDesc(Chapter::getSortOrder)
                        .last("LIMIT 1"));
        chapter.setSortOrder(existing.isEmpty() ? 1 : existing.get(0).getSortOrder() + 1);
        chapterMapper.insert(chapter);
        return chapter;
    }

    @Override
    public Chapter update(Integer id, Chapter chapter) {
        Chapter existing = chapterMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("章节不存在");
        }
        chapter.setId(id);
        chapterMapper.updateById(chapter);
        return chapterMapper.selectById(id);
    }

    @Override
    public void delete(Integer id) {
        Chapter existing = chapterMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("章节不存在");
        }
        // 检查该章节下是否有题目
        Long questionCount = questionMapper.selectCount(
                new LambdaQueryWrapper<Question>().eq(Question::getChapterId, id));
        if (questionCount > 0) {
            throw new BusinessException("该章节下还有 " + questionCount + " 道题目，请先删除题目再删除章节");
        }
        // 检查该章节下是否有错题记录
        Long wrongCount = wrongQuestionMapper.selectCount(
                new LambdaQueryWrapper<WrongQuestion>().eq(WrongQuestion::getChapterId, id));
        if (wrongCount > 0) {
            throw new BusinessException("该章节下还有 " + wrongCount + " 条错题记录，请先清理后再删除章节");
        }
        chapterMapper.deleteById(id);
    }
}
