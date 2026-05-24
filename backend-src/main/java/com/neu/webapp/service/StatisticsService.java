package com.neu.webapp.service;

import java.util.List;
import java.util.Map;

public interface StatisticsService {

    Map<String, Object> getOverview();

    List<Map<String, Object>> getYearlyStats();

    List<Map<String, Object>> getMonthlyStats(int year);

    Map<String, Object> getChartData(int year);

    Map<String, Object> getCourierOverview(Long courierId);
}
