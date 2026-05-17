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

    @Select("SELECT s.id as subjectId, s.name as subjectName, " +
            "COUNT(DISTINCT q.id) as totalQuestions, " +
            "COUNT(DISTINCT ar.question_id) as answeredCount, " +
            "IFNULL(ROUND(SUM(CASE WHEN ar.is_correct = 1 THEN 1 ELSE 0 END) * 100.0 / NULLIF(COUNT(DISTINCT ar.question_id), 0), 1), 0) as accuracy " +
            "FROM subjects s " +
            "LEFT JOIN questions q ON q.subject_id = s.id " +
            "LEFT JOIN answer_records ar ON ar.question_id = q.id " +
            "GROUP BY s.id, s.name ORDER BY s.sort_order")
    List<Map<String, Object>> getSubjectProgressBatch();
}
