package com.examreview.controller;

import com.examreview.dto.ApiResponse;
import com.examreview.dto.StatsOverviewDTO;
import com.examreview.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/overview")
    public ApiResponse<StatsOverviewDTO> getOverview() {
        return ApiResponse.ok(statsService.getOverview());
    }

    @GetMapping("/accuracy/trend")
    public ApiResponse<List<Map<String, Object>>> getAccuracyTrend(@RequestParam(defaultValue = "7") int days) {
        return ApiResponse.ok(statsService.getAccuracyTrend(days));
    }

    @GetMapping("/subject/progress")
    public ApiResponse<List<Map<String, Object>>> getSubjectProgress() {
        return ApiResponse.ok(statsService.getSubjectProgress());
    }

    @GetMapping("/daily/activity")
    public ApiResponse<List<Map<String, Object>>> getDailyActivity(@RequestParam(defaultValue = "30") int days) {
        return ApiResponse.ok(statsService.getDailyActivity(days));
    }
}
