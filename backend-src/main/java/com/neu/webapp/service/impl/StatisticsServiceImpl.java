package com.neu.webapp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neu.webapp.entity.ErrorParcel;
import com.neu.webapp.entity.Parcel;
import com.neu.webapp.entity.SendPackage;
import com.neu.webapp.mapper.ErrorParcelMapper;
import com.neu.webapp.mapper.ParcelMapper;
import com.neu.webapp.mapper.SendPackageMapper;
import com.neu.webapp.service.StatisticsService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final ParcelMapper parcelMapper;
    private final SendPackageMapper sendPackageMapper;
    private final ErrorParcelMapper errorParcelMapper;

    public StatisticsServiceImpl(ParcelMapper parcelMapper, SendPackageMapper sendPackageMapper, ErrorParcelMapper errorParcelMapper) {
        this.parcelMapper = parcelMapper;
        this.sendPackageMapper = sendPackageMapper;
        this.errorParcelMapper = errorParcelMapper;
    }

    @Override
    public Map<String, Object> getOverview() {
        Map<String, Object> stats = new LinkedHashMap<>();
        Long totalInWarehouse = parcelMapper.selectCount(
                new QueryWrapper<Parcel>().eq("status", "IN_WAREHOUSE"));
        Long totalPickedUp = parcelMapper.selectCount(
                new QueryWrapper<Parcel>().eq("status", "PICKED_UP"));
        Long totalParcels = parcelMapper.selectCount(null);
        Long unresolvedErrors = errorParcelMapper.selectCount(
                new QueryWrapper<ErrorParcel>().eq("status", "UNRESOLVED"));
        Long totalSendPackages = sendPackageMapper.selectCount(null);

        stats.put("totalInWarehouse", totalInWarehouse);
        stats.put("totalPickedUp", totalPickedUp);
        stats.put("totalParcels", totalParcels);
        stats.put("unresolvedErrors", unresolvedErrors);
        stats.put("totalSendPackages", totalSendPackages);
        return stats;
    }

    @Override
    public List<Map<String, Object>> getYearlyStats() {
        List<Parcel> parcels = parcelMapper.selectList(null);
        Map<Integer, int[]> yearMap = new TreeMap<>();
        for (Parcel p : parcels) {
            if (p.getEnterTime() != null) {
                int year = p.getEnterTime().getYear();
                yearMap.putIfAbsent(year, new int[2]);
                yearMap.get(year)[0]++;
                if ("PICKED_UP".equals(p.getStatus())) {
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
        List<Parcel> parcels = parcelMapper.selectList(
                new QueryWrapper<Parcel>().ge("enter_time", year + "-01-01").lt("enter_time", (year + 1) + "-01-01"));
        for (Parcel p : parcels) {
            if (p.getEnterTime() != null) {
                int month = p.getEnterTime().getMonthValue() - 1;
                enterCounts[month]++;
                if ("PICKED_UP".equals(p.getStatus())) {
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
        Long pendingPickups = sendPackageMapper.selectCount(
                new QueryWrapper<SendPackage>().eq("status", "PAID"));
        Long todayDeliveries = sendPackageMapper.selectCount(
                new QueryWrapper<SendPackage>()
                        .eq("status", "DELIVERING")
                        .ge("updated_at", java.time.LocalDate.now().toString()));
        Long todayWarehoused = sendPackageMapper.selectCount(
                new QueryWrapper<SendPackage>()
                        .eq("status", "IN_WAREHOUSE")
                        .ge("updated_at", java.time.LocalDate.now().toString()));

        stats.put("pendingPickups", pendingPickups);
        stats.put("todayDeliveries", todayDeliveries);
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
