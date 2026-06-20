package com.examreview.service;

import com.examreview.dto.StatsOverviewDTO;

import java.util.List;
import java.util.Map;

public interface StatsService {
    StatsOverviewDTO getOverview();
    List<Map<String, Object>> getSubjectProgress();
}
