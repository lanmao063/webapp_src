package com.neu.webapp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neu.webapp.entity.ErrorParcel;
import com.neu.webapp.entity.InboundPackage;
import com.neu.webapp.entity.SendPackage;
import com.neu.webapp.mapper.ErrorParcelMapper;
import com.neu.webapp.mapper.InboundPackageMapper;
import com.neu.webapp.mapper.SendPackageMapper;
import com.neu.webapp.service.StatisticsService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final InboundPackageMapper inboundPackageMapper;
    private final SendPackageMapper sendPackageMapper;
    private final ErrorParcelMapper errorParcelMapper;

    public StatisticsServiceImpl(InboundPackageMapper inboundPackageMapper,
                                 SendPackageMapper sendPackageMapper,
                                 ErrorParcelMapper errorParcelMapper) {
        this.inboundPackageMapper = inboundPackageMapper;
        this.sendPackageMapper = sendPackageMapper;
        this.errorParcelMapper = errorParcelMapper;
    }

    @Override
    public Map<String, Object> getOverview() {
        Map<String, Object> stats = new LinkedHashMap<>();
        Long totalInWarehouse = inboundPackageMapper.selectCount(
                new QueryWrapper<InboundPackage>().eq("status", "IN_WAREHOUSE"));
        Long totalCheckedOut = inboundPackageMapper.selectCount(
                new QueryWrapper<InboundPackage>().eq("status", "CHECKED_OUT"));
        Long totalInbound = inboundPackageMapper.selectCount(null);
        Long unresolvedErrors = errorParcelMapper.selectCount(
                new QueryWrapper<ErrorParcel>().eq("status", "UNRESOLVED"));
        Long totalSendPackages = sendPackageMapper.selectCount(null);

        stats.put("totalInWarehouse", totalInWarehouse);
        stats.put("totalPickedUp", totalCheckedOut);
        stats.put("totalParcels", totalInbound);
        stats.put("unresolvedErrors", unresolvedErrors);
        stats.put("totalSendPackages", totalSendPackages);
        return stats;
    }

    @Override
    public List<Map<String, Object>> getYearlyStats() {
        List<InboundPackage> inbounds = inboundPackageMapper.selectList(null);
        Map<Integer, int[]> yearMap = new TreeMap<>();
        for (InboundPackage ib : inbounds) {
            if (ib.getEnterTime() != null) {
                int year = ib.getEnterTime().getYear();
                yearMap.putIfAbsent(year, new int[2]);
                yearMap.get(year)[0]++;
                if ("CHECKED_OUT".equals(ib.getStatus())) {
                    yearMap.get(year)[1]++;
                }
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Integer, int[]> entry : yearMap.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("year", entry.getKey());
            item.put("enterCount", entry.getValue()[0]);
            item.put("pickupCount", entry.getValue()[1]);
            result.add(item);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getMonthlyStats(int year) {
        int[] enterCounts = new int[12];
        int[] pickupCounts = new int[12];
        List<InboundPackage> inbounds = inboundPackageMapper.selectList(
                new QueryWrapper<InboundPackage>()
                        .ge("enter_time", year + "-01-01")
                        .lt("enter_time", (year + 1) + "-01-01"));
        for (InboundPackage ib : inbounds) {
            if (ib.getEnterTime() != null) {
                int month = ib.getEnterTime().getMonthValue() - 1;
                enterCounts[month]++;
                if ("CHECKED_OUT".equals(ib.getStatus())) {
                    pickupCounts[month]++;
                }
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("month", (i + 1) + "月");
            item.put("enterCount", enterCounts[i]);
            item.put("pickupCount", pickupCounts[i]);
            result.add(item);
        }
        return result;
    }

    @Override
    public Map<String, Object> getCourierOverview(Long courierId) {
        Map<String, Object> stats = new LinkedHashMap<>();
        Long todayCollected = sendPackageMapper.selectCount(
                new QueryWrapper<SendPackage>()
                        .eq("status", "COLLECTED")
                        .eq("courier_id", courierId)
                        .ge("updated_at", LocalDate.now().toString()));
        Long todayWarehoused = inboundPackageMapper.selectCount(
                new QueryWrapper<InboundPackage>()
                        .eq("status", "IN_WAREHOUSE")
                        .eq("entered_by", courierId)
                        .ge("enter_time", LocalDate.now().toString()));

        stats.put("todayDeliveries", todayCollected);
        stats.put("todayWarehoused", todayWarehoused);
        return stats;
    }

    @Override
    public Map<String, Object> getChartData(int year) {
        List<Map<String, Object>> monthly = getMonthlyStats(year);
        List<String> labels = new ArrayList<>();
        List<Integer> enterData = new ArrayList<>();
        List<Integer> pickupData = new ArrayList<>();
        for (Map<String, Object> m : monthly) {
            labels.add((String) m.get("month"));
            enterData.add((Integer) m.get("enterCount"));
            pickupData.add((Integer) m.get("pickupCount"));
        }
        Map<String, Object> chart = new LinkedHashMap<>();
        chart.put("labels", labels);
        chart.put("enterData", enterData);
        chart.put("pickupData", pickupData);
        return chart;
    }
}
