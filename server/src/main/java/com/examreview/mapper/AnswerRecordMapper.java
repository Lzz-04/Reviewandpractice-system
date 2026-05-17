package com.examreview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.examreview.entity.AnswerRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface AnswerRecordMapper extends BaseMapper<AnswerRecord> {

    @Select("SELECT DATE(answered_at) as date, COUNT(*) as answered, " +
            "SUM(is_correct) as correct, " +
            "ROUND(SUM(is_correct) * 100.0 / COUNT(*), 1) as accuracy " +
            "FROM answer_records " +
            "WHERE answered_at >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
            "GROUP BY DATE(answered_at) ORDER BY date")
    List<Map<String, Object>> getAccuracyTrend(@Param("days") int days);

    @Select("SELECT DATE(answered_at) as date, COUNT(*) as count " +
            "FROM answer_records " +
            "WHERE answered_at >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
            "GROUP BY DATE(answered_at) ORDER BY date")
    List<Map<String, Object>> getDailyActivity(@Param("days") int days);

    @Select("SELECT COUNT(DISTINCT DATE(answered_at)) FROM answer_records")
    Long countStudyDays();

    @Select("SELECT COUNT(DISTINCT question_id) FROM answer_records " +
            "WHERE question_id IN (SELECT id FROM questions WHERE subject_id = #{subjectId})")
    Long countDistinctQuestionsBySubject(@Param("subjectId") Integer subjectId);
}
