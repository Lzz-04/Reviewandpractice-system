package com.examreview.service;

import com.examreview.dto.StatsOverviewDTO;

import java.util.List;
import java.util.Map;

public interface StatsService {
    StatsOverviewDTO getOverview();
    List<Map<String, Object>> getAccuracyTrend(int days);
    List<Map<String, Object>> getSubjectProgress();
    List<Map<String, Object>> getDailyActivity(int days);
}
