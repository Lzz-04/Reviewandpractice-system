package com.examreview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.examreview.dto.WrongQuestionDTO;
import com.examreview.entity.Question;
import com.examreview.entity.WrongQuestion;
import com.examreview.exception.BusinessException;
import com.examreview.mapper.QuestionMapper;
import com.examreview.mapper.WrongQuestionMapper;
import com.examreview.service.WrongBookService;
import com.examreview.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WrongBookServiceImpl implements WrongBookService {

    private final WrongQuestionMapper wrongQuestionMapper;
    private final QuestionMapper questionMapper;

    @Override
    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<WrongQuestionDTO> getList(
            Integer page, Integer pageSize, Integer subjectId, Integer mastered, Long userId) {
        if (page == null || page < 1) page = 1;
        if (pageSize == null || pageSize < 1) pageSize = 20;

        LambdaQueryWrapper<WrongQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(!SecurityUtil.isAdmin(), WrongQuestion::getUserId, userId);
        if (subjectId != null) {
            wrapper.eq(WrongQuestion::getSubjectId, subjectId);
        }
        if (mastered != null) {
            wrapper.eq(WrongQuestion::getMastered, mastered);
        }
        wrapper.orderByDesc(WrongQuestion::getLastWrongAt);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<WrongQuestion> mpPage =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, pageSize);
        mpPage = wrongQuestionMapper.selectPage(mpPage, wrapper);
        List<WrongQuestion> list = mpPage.getRecords();

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<WrongQuestionDTO> resultPage =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, pageSize, mpPage.getTotal());

        if (list.isEmpty()) {
            resultPage.setRecords(java.util.Collections.emptyList());
            return resultPage;
        }

        // 批量加载关联的题目
        List<Integer> questionIds = list.stream().map(WrongQuestion::getQuestionId).collect(Collectors.toList());
        List<Question> questions = questionMapper.selectBatchIds(questionIds);
        Map<Integer, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        List<WrongQuestionDTO> result = new ArrayList<>();
        for (WrongQuestion wq : list) {
            WrongQuestionDTO dto = new WrongQuestionDTO();
            dto.setId(wq.getId());
            dto.setQuestionId(wq.getQuestionId());
            dto.setSubjectId(wq.getSubjectId());
            dto.setChapterId(wq.getChapterId());
            dto.setWrongCount(wq.getWrongCount());
            dto.setLastWrongAt(wq.getLastWrongAt());
            dto.setReviewedCount(wq.getReviewedCount());
            dto.setMastered(wq.getMastered());

            Question q = questionMap.get(wq.getQuestionId());
            if (q != null) {
                dto.setType(q.getType());
                dto.setContent(q.getContent());
                dto.setOptions(q.getOptions());
                dto.setAnswer(q.getAnswer());
                dto.setAnalysis(q.getAnalysis());
                dto.setDifficulty(q.getDifficulty());
            }
            result.add(dto);
        }
        resultPage.setRecords(result);
        return resultPage;
    }

    @Override
    public Map<String, Object> getStats(Long userId) {
        long total = wrongQuestionMapper.selectCount(
                new LambdaQueryWrapper<WrongQuestion>().eq(!SecurityUtil.isAdmin(), WrongQuestion::getUserId, userId));
        long masteredCount = wrongQuestionMapper.selectCount(
                new LambdaQueryWrapper<WrongQuestion>()
                        .eq(!SecurityUtil.isAdmin(), WrongQuestion::getUserId, userId)
                        .eq(WrongQuestion::getMastered, 1));
        return Map.of("total", total, "mastered", masteredCount, "unMastered", total - masteredCount);
    }

    @Override
    public void review(Integer id, Long userId) {
        WrongQuestion wq = wrongQuestionMapper.selectById(id);
        if (wq == null) {
            throw new BusinessException("错题记录不存在");
        }
        if (!SecurityUtil.isAdmin() && !wq.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此错题记录");
        }
        wq.setReviewedCount(wq.getReviewedCount() + 1);
        wrongQuestionMapper.updateById(wq);
    }

    @Override
    public void master(Integer id, Long userId) {
        WrongQuestion wq = wrongQuestionMapper.selectById(id);
        if (wq == null) {
            throw new BusinessException("错题记录不存在");
        }
        if (!SecurityUtil.isAdmin() && !wq.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此错题记录");
        }
        wq.setMastered(wq.getMastered() == 1 ? 0 : 1);
        wrongQuestionMapper.updateById(wq);
    }

    @Override
    public void remove(Integer id, Long userId) {
        WrongQuestion wq = wrongQuestionMapper.selectById(id);
        if (wq == null) {
            throw new BusinessException("错题记录不存在");
        }
        if (!SecurityUtil.isAdmin() && !wq.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此错题记录");
        }
        wrongQuestionMapper.deleteById(id);
    }

    @Override
    public void upsertWrongQuestion(Question question, Long userId) {
        WrongQuestion existing = wrongQuestionMapper.selectOne(
                new LambdaQueryWrapper<WrongQuestion>()
                        .eq(!SecurityUtil.isAdmin(), WrongQuestion::getUserId, userId)
                        .eq(WrongQuestion::getQuestionId, question.getId()));

        if (existing != null) {
            existing.setWrongCount(existing.getWrongCount() + 1);
            existing.setLastWrongAt(java.time.LocalDateTime.now());
            wrongQuestionMapper.updateById(existing);
        } else {
            WrongQuestion wq = new WrongQuestion();
            wq.setQuestionId(question.getId());
            wq.setSubjectId(question.getSubjectId());
            wq.setChapterId(question.getChapterId());
            wq.setWrongCount(1);
            wq.setUserId(userId);
            wq.setLastWrongAt(java.time.LocalDateTime.now());
            wq.setReviewedCount(0);
            wq.setMastered(0);
            wrongQuestionMapper.insert(wq);
        }
    }
}
