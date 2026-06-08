package com.examreview.controller;

import com.examreview.dto.ApiResponse;
import com.examreview.dto.WrongQuestionDTO;
import com.examreview.entity.Question;
import com.examreview.entity.WrongQuestion;
import com.examreview.exception.BusinessException;
import com.examreview.mapper.QuestionMapper;
import com.examreview.mapper.WrongQuestionMapper;
import com.examreview.service.AIService;
import com.examreview.service.WrongBookService;
import com.examreview.util.SecurityUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/wrongbook")
@RequiredArgsConstructor
public class WrongBookController {

    private final WrongBookService wrongBookService;
    private final AIService aiService;
    private final WrongQuestionMapper wrongQuestionMapper;
    private final QuestionMapper questionMapper;

    @GetMapping
    public ApiResponse<Page<WrongQuestionDTO>> getList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Integer subjectId,
            @RequestParam(required = false) Integer mastered) {
        return ApiResponse.ok(wrongBookService.getList(page, pageSize, subjectId, mastered, SecurityUtil.getCurrentUserId()));
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getStats() {
        return ApiResponse.ok(wrongBookService.getStats(SecurityUtil.getCurrentUserId()));
    }

    @PostMapping("/{id}/review")
    public ApiResponse<Void> review(@PathVariable Integer id) {
        wrongBookService.review(id, SecurityUtil.getCurrentUserId());
        return ApiResponse.ok(null, "复习标记成功");
    }

    @PostMapping("/{id}/master")
    public ApiResponse<Void> master(@PathVariable Integer id) {
        wrongBookService.master(id, SecurityUtil.getCurrentUserId());
        return ApiResponse.ok(null, "操作成功");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> remove(@PathVariable Integer id) {
        wrongBookService.remove(id, SecurityUtil.getCurrentUserId());
        return ApiResponse.ok(null, "已从错题本移除");
    }

    /** AI 错题解析 */
    @GetMapping("/{id}/ai-analysis")
    public ApiResponse<String> getAIAnalysis(@PathVariable Integer id) {
        WrongQuestion wq = wrongQuestionMapper.selectById(id);
        if (wq == null) throw new BusinessException("错题不存在");
        Question q = questionMapper.selectById(wq.getQuestionId());
        if (q == null) throw new BusinessException("题目不存在");

        String system = "你是一位专业的学科辅导老师，擅长用通俗易懂的方式讲解题目。请用中文回答。";
        String user = String.format("""
                请详细讲解以下题目，包括解题思路、为什么正确答案是对的、常见错误选项分析、以及避免再次出错的建议（200字以内）。

                题型：%s
                题目：%s
                选项：%s
                正确答案：%s
                解析：%s
                """,
                q.getType(), q.getContent(), q.getOptions(), q.getAnswer(),
                q.getAnalysis() != null ? q.getAnalysis() : "无");

        return ApiResponse.ok(aiService.analyze(system, user));
    }
}
