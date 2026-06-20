package com.examreview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.examreview.dto.StatsOverviewDTO;
import com.examreview.entity.AnswerRecord;
import com.examreview.mapper.AnswerRecordMapper;
import com.examreview.mapper.QuestionMapper;
import com.examreview.service.StatsService;
import com.examreview.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final AnswerRecordMapper answerRecordMapper;
    private final QuestionMapper questionMapper;

    @Override
    public StatsOverviewDTO getOverview() {
        Long userId = SecurityUtil.getCurrentUserId();

        StatsOverviewDTO dto = new StatsOverviewDTO();
        dto.setTotalQuestions(questionMapper.selectCount(new LambdaQueryWrapper<com.examreview.entity.Question>()
                .eq(com.examreview.entity.Question::getUserId, userId)));

        LambdaQueryWrapper<AnswerRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AnswerRecord::getUserId, userId);
        dto.setTotalAnswered(answerRecordMapper.selectCount(wrapper));

        LambdaQueryWrapper<AnswerRecord> correctWrapper = new LambdaQueryWrapper<>();
        correctWrapper.eq(AnswerRecord::getUserId, userId);
        correctWrapper.eq(AnswerRecord::getIsCorrect, 1);
        Long correctCount = answerRecordMapper.selectCount(correctWrapper);

        if (dto.getTotalAnswered() > 0) {
            double accuracy = correctCount * 100.0 / dto.getTotalAnswered();
            dto.setOverallAccuracy(Math.round(accuracy * 10.0) / 10.0);
        } else {
            dto.setOverallAccuracy(0.0);
        }

        LambdaQueryWrapper<AnswerRecord> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.eq(AnswerRecord::getUserId, userId);
        todayWrapper.apply("DATE(answered_at) = CURDATE()");
        dto.setTodayAnswered(answerRecordMapper.selectCount(todayWrapper));

        Long studyDays = answerRecordMapper.countStudyDays(userId);
        dto.setStudyDays(studyDays != null ? studyDays.intValue() : 0);

        return dto;
    }

    @Override
    public List<Map<String, Object>> getSubjectProgress() {
        return answerRecordMapper.getSubjectProgressBatch(SecurityUtil.getCurrentUserId());
    }
}
