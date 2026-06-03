package com.examreview.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.examreview.dto.ApiResponse;
import com.examreview.entity.*;
import com.examreview.exception.BusinessException;
import com.examreview.mapper.*;
import com.examreview.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserMapper userMapper;
    private final SubjectMapper subjectMapper;
    private final ChapterMapper chapterMapper;
    private final QuestionMapper questionMapper;
    private final ExamPaperMapper examPaperMapper;
    private final ExamQuestionMapper examQuestionMapper;
    private final ExamRecordMapper examRecordMapper;
    private final WrongQuestionMapper wrongQuestionMapper;
    private final AnswerRecordMapper answerRecordMapper;

    /**
     * 获取所有用户列表及统计数据
     */
    @GetMapping("/users")
    public ApiResponse<List<Map<String, Object>>> getUsers() {
        if (!SecurityUtil.isAdmin()) {
            throw new BusinessException(403, "无权访问");
        }
        List<User> users = userMapper.selectList(null);
        List<Map<String, Object>> result = new ArrayList<>();
        for (User u : users) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", u.getId());
            item.put("username", u.getUsername());
            item.put("nickname", u.getNickname());
            item.put("role", u.getRole());
            item.put("createdAt", u.getCreatedAt());
            item.put("subjectCount", subjectMapper.selectCount(
                    new LambdaQueryWrapper<Subject>().eq(Subject::getUserId, u.getId())));
            item.put("questionCount", questionMapper.selectCount(
                    new LambdaQueryWrapper<Question>().eq(Question::getUserId, u.getId())));
            item.put("examCount", examRecordMapper.selectCount(
                    new LambdaQueryWrapper<ExamRecord>().eq(ExamRecord::getUserId, u.getId())));
            item.put("wrongCount", wrongQuestionMapper.selectCount(
                    new LambdaQueryWrapper<WrongQuestion>().eq(WrongQuestion::getUserId, u.getId())));
            item.put("answerCount", answerRecordMapper.selectCount(
                    new LambdaQueryWrapper<AnswerRecord>().eq(AnswerRecord::getUserId, u.getId())));
            result.add(item);
        }
        return ApiResponse.ok(result);
    }

    /**
     * 删除用户及其所有关联数据
     */
    @DeleteMapping("/users/{id}")
    @Transactional
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        if (!SecurityUtil.isAdmin()) {
            throw new BusinessException(403, "无权访问");
        }
        if (SecurityUtil.getCurrentUserId().equals(id)) {
            throw new BusinessException("不能删除自己的账号");
        }
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if ("admin".equals(user.getRole())) {
            throw new BusinessException("不能删除管理员账号");
        }

        // 删除答题记录
        answerRecordMapper.delete(
                new LambdaQueryWrapper<AnswerRecord>().eq(AnswerRecord::getUserId, id));
        // 删除错题本
        wrongQuestionMapper.delete(
                new LambdaQueryWrapper<WrongQuestion>().eq(WrongQuestion::getUserId, id));
        // 删除考试记录
        examRecordMapper.delete(
                new LambdaQueryWrapper<ExamRecord>().eq(ExamRecord::getUserId, id));

        // 删除试卷及关联题目
        List<ExamPaper> papers = examPaperMapper.selectList(
                new LambdaQueryWrapper<ExamPaper>().eq(ExamPaper::getUserId, id));
        for (ExamPaper p : papers) {
            examQuestionMapper.delete(
                    new LambdaQueryWrapper<ExamQuestion>().eq(ExamQuestion::getExamId, p.getId()));
            examPaperMapper.deleteById(p.getId());
        }

        // 删除题目
        questionMapper.delete(
                new LambdaQueryWrapper<Question>().eq(Question::getUserId, id));
        // 删除章节
        chapterMapper.delete(
                new LambdaQueryWrapper<Chapter>().eq(Chapter::getUserId, id));
        // 删除科目
        subjectMapper.delete(
                new LambdaQueryWrapper<Subject>().eq(Subject::getUserId, id));
        // 删除用户
        userMapper.deleteById(id);

        return ApiResponse.ok(null, "用户及其数据已删除");
    }
}
