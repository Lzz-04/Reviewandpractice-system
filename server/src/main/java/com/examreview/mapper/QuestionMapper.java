package com.examreview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.examreview.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {

    @Select("SELECT * FROM questions WHERE subject_id = #{subjectId} AND type IN ('single','multiple','judge')")
    List<Question> selectBySubject(@Param("subjectId") Integer subjectId);

    @Select("SELECT * FROM questions WHERE chapter_id = #{chapterId}")
    List<Question> selectByChapter(@Param("chapterId") Integer chapterId);
}
