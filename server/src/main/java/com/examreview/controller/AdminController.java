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

/**
 * 管理后台控制器
 * 提供管理员专属功能，包括用户列表查询和用户删除（级联清理所有关联数据）
 * 所有接口均需 admin 角色权限
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserMapper userMapper;
    private final SubjectMapper subjectMapper;
    private final QuestionMapper questionMapper;
    private final ExamPaperMapper examPaperMapper;
    private final ExamQuestionMapper examQuestionMapper;
    private final ExamRecordMapper examRecordMapper;
    private final WrongQuestionMapper wrongQuestionMapper;
    private final AnswerRecordMapper answerRecordMapper;

    /**
     * 获取所有用户列表及统计数据
     * 包含每个用户的科目数、题目数、考试数、错题数、答题记录数
     *
     * @return 用户列表及统计信息
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
     * 级联删除顺序：答题记录 → 错题本 → 考试记录 → 试卷及关联题目 → 题目 → 科目 → 用户
     *
     * @param id 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/users/{id}")
    @Transactional
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        // 权限校验
        if (!SecurityUtil.isAdmin()) {
            throw new BusinessException(403, "无权访问");
        }
        // 禁止删除自己
        if (SecurityUtil.getCurrentUserId().equals(id)) {
            throw new BusinessException("不能删除自己的账号");
        }
        // 用户存在性检查
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 禁止删除管理员
        if ("admin".equals(user.getRole())) {
            throw new BusinessException("不能删除管理员账号");
        }

        // 级联删除答题记录
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
        // 删除科目
        subjectMapper.delete(
                new LambdaQueryWrapper<Subject>().eq(Subject::getUserId, id));
        // 删除用户
        userMapper.deleteById(id);

        return ApiResponse.ok(null, "用户及其数据已删除");
    }
}
