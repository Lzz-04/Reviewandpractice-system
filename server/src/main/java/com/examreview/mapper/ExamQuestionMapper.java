package com.examreview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.examreview.entity.ExamQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ExamQuestionMapper extends BaseMapper<ExamQuestion> {

    @Select("SELECT eq.* FROM exam_questions eq WHERE eq.exam_id = #{examId} ORDER BY eq.sort_order")
    List<ExamQuestion> selectByExamId(@Param("examId") Integer examId);
}
