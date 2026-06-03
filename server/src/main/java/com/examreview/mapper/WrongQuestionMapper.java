package com.examreview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.examreview.entity.WrongQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface WrongQuestionMapper extends BaseMapper<WrongQuestion> {

    @Select("SELECT s.name as subjectName, COUNT(w.id) as count " +
            "FROM wrong_questions w JOIN subjects s ON w.subject_id = s.id " +
            "WHERE w.mastered = 0 AND w.user_id = #{userId} GROUP BY w.subject_id")
    List<Map<String, Object>> getUnMasteredDistribution(@Param("userId") Long userId);
}
