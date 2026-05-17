package com.examreview.service.impl;

import com.examreview.dto.StatsOverviewDTO;
import com.examreview.entity.Question;
import com.examreview.mapper.AnswerRecordMapper;
import com.examreview.mapper.QuestionMapper;
import com.examreview.mapper.SubjectMapper;
import com.examreview.mapper.WrongQuestionMapper;
import com.examreview.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final AnswerRecordMapper answerRecordMapper;
    private final QuestionMapper questionMapper;
    private final SubjectMapper subjectMapper;
    private final WrongQuestionMapper wrongQuestionMapper;

    @Override
    public StatsOverviewDTO getOverview() {
        StatsOverviewDTO dto = new StatsOverviewDTO();
        dto.setTotalQuestions(questionMapper.selectCount(null));
        dto.setTotalAnswered(answerRecordMapper.selectCount(null));

        // 整体正确率
        Long correctCount = answerRecordMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.examreview.entity.AnswerRecord>()
                        .eq(com.examreview.entity.AnswerRecord::getIsCorrect, 1));
        if (dto.getTotalAnswered() > 0) {
            double accuracy = correctCount * 100.0 / dto.getTotalAnswered();
            dto.setOverallAccuracy(Math.round(accuracy * 10.0) / 10.0);
        } else {
            dto.setOverallAccuracy(0.0);
        }

        // 今日答题数
        Long todayCount = answerRecordMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.examreview.entity.AnswerRecord>()
                        .apply("DATE(answered_at) = CURDATE()"));
        dto.setTodayAnswered(todayCount);
        Long studyDays = answerRecordMapper.countStudyDays();
        dto.setStudyDays(studyDays != null ? studyDays.intValue() : 0);

        return dto;
    }

    @Override
    public List<Map<String, Object>> getAccuracyTrend(int days) {
        return answerRecordMapper.getAccuracyTrend(days);
    }

    @Override
    public List<Map<String, Object>> getSubjectProgress() {
        List<Map<String, Object>> result = new ArrayList<>();
        // 按科目统计
        List<com.examreview.entity.Subject> subjects = subjectMapper.selectList(null);
        for (com.examreview.entity.Subject subject : subjects) {
            Map<String, Object> item = new HashMap<>();
            item.put("subjectId", subject.getId());
            item.put("subjectName", subject.getName());
            Long totalQuestions = questionMapper.selectCount(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Question>()
                            .eq(Question::getSubjectId, subject.getId()));
            item.put("totalQuestions", totalQuestions);

            Long answeredCount = answerRecordMapper.countDistinctQuestionsBySubject(subject.getId());
            item.put("answeredCount", answeredCount);

            Long correctCount = answerRecordMapper.selectCount(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.examreview.entity.AnswerRecord>()
                            .eq(com.examreview.entity.AnswerRecord::getIsCorrect, 1)
                            .apply("question_id IN (SELECT id FROM questions WHERE subject_id = {0})", subject.getId()));
            double accuracy = answeredCount > 0 ? correctCount * 100.0 / answeredCount : 0;
            item.put("accuracy", Math.round(accuracy * 10.0) / 10.0);
            result.add(item);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getDailyActivity(int days) {
        return answerRecordMapper.getDailyActivity(days);
    }
}
