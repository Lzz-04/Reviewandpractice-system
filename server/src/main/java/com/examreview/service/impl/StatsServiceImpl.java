package com.examreview.service.impl;

import com.examreview.dto.StatsOverviewDTO;
import com.examreview.mapper.AnswerRecordMapper;
import com.examreview.mapper.QuestionMapper;
import com.examreview.mapper.WrongQuestionMapper;
import com.examreview.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final AnswerRecordMapper answerRecordMapper;
    private final QuestionMapper questionMapper;
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
        return answerRecordMapper.getSubjectProgressBatch();
    }

    @Override
    public List<Map<String, Object>> getDailyActivity(int days) {
        return answerRecordMapper.getDailyActivity(days);
    }
}
