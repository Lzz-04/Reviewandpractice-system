package com.examreview.controller;

import com.examreview.dto.ApiResponse;
import com.examreview.dto.StatsOverviewDTO;
import com.examreview.mapper.WrongQuestionMapper;
import com.examreview.service.AIService;
import com.examreview.service.StatsService;
import com.examreview.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;
    private final AIService aiService;
    private final WrongQuestionMapper wrongQuestionMapper;

    @GetMapping("/overview")
    public ApiResponse<StatsOverviewDTO> getOverview() {
        return ApiResponse.ok(statsService.getOverview());
    }

    @GetMapping("/accuracy/trend")
    public ApiResponse<List<Map<String, Object>>> getAccuracyTrend(@RequestParam(defaultValue = "7") int days) {
        if (days <= 0) return ApiResponse.fail("查询天数必须大于0");
        return ApiResponse.ok(statsService.getAccuracyTrend(days));
    }

    @GetMapping("/subject/progress")
    public ApiResponse<List<Map<String, Object>>> getSubjectProgress() {
        return ApiResponse.ok(statsService.getSubjectProgress());
    }

    @GetMapping("/daily/activity")
    public ApiResponse<List<Map<String, Object>>> getDailyActivity(@RequestParam(defaultValue = "30") int days) {
        if (days <= 0) return ApiResponse.fail("查询天数必须大于0");
        return ApiResponse.ok(statsService.getDailyActivity(days));
    }

    /** AI 学习分析 */
    @GetMapping("/ai-analysis")
    public ApiResponse<String> getAIAnalysis() {
        Long userId = SecurityUtil.isAdmin() ? null : SecurityUtil.getCurrentUserId();

        // 收集统计数据
        StatsOverviewDTO overview = statsService.getOverview();
        List<Map<String, Object>> trend = statsService.getAccuracyTrend(7);
        List<Map<String, Object>> progress = statsService.getSubjectProgress();
        List<Map<String, Object>> weakAreas = wrongQuestionMapper.getUnMasteredDistribution(userId);

        // 构建提示词
        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append("请根据以下学习数据进行分析，并给出个性化的学习建议：\n\n");

        userPrompt.append("【概览】\n");
        userPrompt.append("题库总题数：").append(overview.getTotalQuestions()).append("\n");
        userPrompt.append("已答题数：").append(overview.getTotalAnswered()).append("\n");
        userPrompt.append("整体正确率：").append(overview.getOverallAccuracy()).append("%\n");
        userPrompt.append("学习天数：").append(overview.getStudyDays()).append(" 天\n");
        userPrompt.append("今日答题数：").append(overview.getTodayAnswered()).append("\n\n");

        if (!trend.isEmpty()) {
            userPrompt.append("【近7天正确率趋势】\n");
            for (Map<String, Object> t : trend) {
                userPrompt.append(t.get("date")).append(": 正确率 ").append(t.get("accuracy")).append("%\n");
            }
            userPrompt.append("\n");
        }

        if (!progress.isEmpty()) {
            userPrompt.append("【各科目进度】\n");
            for (Map<String, Object> p : progress) {
                userPrompt.append(p.get("subjectName")).append(": 已答 ")
                        .append(p.get("answeredCount")).append("/")
                        .append(p.get("totalQuestions")).append(" 题，正确率 ")
                        .append(p.get("accuracy")).append("%\n");
            }
            userPrompt.append("\n");
        }

        if (!weakAreas.isEmpty()) {
            userPrompt.append("【薄弱科目（未掌握错题分布）】\n");
            for (Map<String, Object> w : weakAreas) {
                userPrompt.append(w.get("subjectName")).append(": ").append(w.get("count")).append(" 道未掌握\n");
            }
            userPrompt.append("\n");
        }

        userPrompt.append("请给出：1. 整体评价 2. 优势科目 3. 薄弱环节 4. 具体学习建议。用通俗易懂的中文回答，300字以内，分点说明。");

        String systemPrompt = "你是一位专业的大学学习辅导专家，擅长分析学生的学习数据并给出针对性的学习建议。请根据数据给出真诚、有建设性的分析和建议。";

        String analysis = aiService.analyze(systemPrompt, userPrompt.toString());
        return ApiResponse.ok(analysis);
    }

    /** AI 复习计划 */
    @GetMapping("/study-plan")
    public ApiResponse<String> getStudyPlan() {
        Long userId = SecurityUtil.isAdmin() ? null : SecurityUtil.getCurrentUserId();
        StatsOverviewDTO overview = statsService.getOverview();
        List<Map<String, Object>> weakAreas = wrongQuestionMapper.getUnMasteredDistribution(userId);
        List<Map<String, Object>> progress = statsService.getSubjectProgress();

        StringBuilder sb = new StringBuilder();
        sb.append("学习概览：正确率").append(overview.getOverallAccuracy()).append("%，")
          .append("已学").append(overview.getStudyDays()).append("天，今日已答").append(overview.getTodayAnswered()).append("题。\n\n");
        sb.append("各科进度：\n");
        for (Map<String, Object> p : progress) {
            sb.append(p.get("subjectName")).append(": 正确率").append(p.get("accuracy")).append("%\n");
        }
        if (!weakAreas.isEmpty()) {
            sb.append("\n薄弱科目（未掌握错题数）：\n");
            for (Map<String, Object> w : weakAreas) {
                sb.append(w.get("subjectName")).append(": ").append(w.get("count")).append("道\n");
            }
        }

        String system = "你是一位学习规划师。请根据数据制定一个为期3天的复习计划，每天标注重点科目和预计用时，200字以内。";
        return ApiResponse.ok(aiService.analyze(system, sb.toString()));
    }
}
