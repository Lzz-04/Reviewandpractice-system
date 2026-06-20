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

    @Select("SELECT COUNT(DISTINCT DATE(answered_at)) FROM answer_records WHERE user_id = #{userId}")
    Long countStudyDays(@Param("userId") Long userId);

    @Select("<script>" +
            "SELECT s.id as subjectId, s.name as subjectName, " +
            "COUNT(DISTINCT q.id) as totalQuestions, " +
            "COUNT(DISTINCT ar.question_id) as answeredCount, " +
            "IFNULL(ROUND(SUM(CASE WHEN ar.is_correct = 1 THEN 1 ELSE 0 END) * 100.0 / NULLIF(COUNT(DISTINCT ar.question_id), 0), 1), 0) as accuracy " +
            "FROM subjects s " +
            "LEFT JOIN questions q ON q.subject_id = s.id " +
            "LEFT JOIN answer_records ar ON ar.question_id = q.id AND ar.user_id = #{userId} " +
            "WHERE s.user_id = #{userId} " +
            "GROUP BY s.id, s.name ORDER BY s.sort_order" +
            "</script>")
    List<Map<String, Object>> getSubjectProgressBatch(@Param("userId") Long userId);
}
