package com.neu.webapp.controller;

import com.neu.webapp.common.Result;
import com.neu.webapp.security.UserContext;
import com.neu.webapp.service.StatisticsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        return Result.ok(statisticsService.getOverview());
    }

    @GetMapping("/yearly")
    public Result<List<Map<String, Object>>> yearly() {
        return Result.ok(statisticsService.getYearlyStats());
    }

    @GetMapping("/monthly")
    public Result<List<Map<String, Object>>> monthly(@RequestParam(defaultValue = "2026") int year) {
        return Result.ok(statisticsService.getMonthlyStats(year));
    }

    @GetMapping("/chart")
    public Result<Map<String, Object>> chart(@RequestParam(defaultValue = "2026") int year) {
        return Result.ok(statisticsService.getChartData(year));
    }

    @GetMapping("/courier-overview")
    public Result<Map<String, Object>> courierOverview() {
        return Result.ok(statisticsService.getCourierOverview(UserContext.getCurrentUserId()));
    }
}
