package com.neu.webapp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neu.webapp.entity.InboundPackage;
import com.neu.webapp.entity.Package;
import com.neu.webapp.entity.SendPackage;
import com.neu.webapp.mapper.InboundPackageMapper;
import com.neu.webapp.mapper.PackageMapper;
import com.neu.webapp.mapper.SendPackageMapper;
import com.neu.webapp.service.PackageService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PackageServiceImpl extends ServiceImpl<PackageMapper, Package> implements PackageService {

    private final InboundPackageMapper inboundPackageMapper;
    private final SendPackageMapper sendPackageMapper;

    public PackageServiceImpl(InboundPackageMapper inboundPackageMapper, SendPackageMapper sendPackageMapper) {
        this.inboundPackageMapper = inboundPackageMapper;
        this.sendPackageMapper = sendPackageMapper;
    }

    @Override
    public Map<String, Object> track(String trackingNumber) {
        Package pkg = baseMapper.selectOne(
                new QueryWrapper<Package>().eq("tracking_number", trackingNumber));
        if (pkg == null) {
            return null;
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", pkg.getId());
        result.put("trackingNumber", pkg.getTrackingNumber());
        result.put("packageName", pkg.getPackageName());
        result.put("weight", pkg.getWeight());
        result.put("volume", pkg.getVolume());
        result.put("senderName", pkg.getSenderName());
        result.put("senderPhone", pkg.getSenderPhone());
        result.put("senderAddress", pkg.getSenderAddress());
        result.put("receiverName", pkg.getReceiverName());
        result.put("receiverPhone", pkg.getReceiverPhone());
        result.put("receiverAddress", pkg.getReceiverAddress());
        result.put("notes", pkg.getNotes());
        result.put("createdAt", pkg.getCreatedAt());

        // Check inbound status
        InboundPackage inbound = inboundPackageMapper.selectOne(
                new QueryWrapper<InboundPackage>().eq("package_id", pkg.getId()));
        if (inbound != null) {
            result.put("inboundStatus", inbound.getStatus());
            result.put("cabinetType", inbound.getCabinetType());
            result.put("pickupCode", inbound.getPickupCode());
            result.put("enterTime", inbound.getEnterTime());
            result.put("outTime", inbound.getOutTime());
        }

        // Check send status
        SendPackage sp = sendPackageMapper.selectOne(
                new QueryWrapper<SendPackage>().eq("package_id", pkg.getId()));
        if (sp != null) {
            result.put("sendStatus", sp.getStatus());
            result.put("fee", sp.getFee());
        }

        return result;
    }

    @Override
    public IPage<Map<String, Object>> search(String keyword, int page, int size) {
        QueryWrapper<Package> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                .like("tracking_number", keyword)
                .or().like("receiver_name", keyword)
                .or().like("receiver_phone", keyword)
                .or().like("sender_name", keyword)
                .or().like("sender_phone", keyword));
        }
        wrapper.orderByDesc("created_at");
        IPage<Package> pkgPage = baseMapper.selectPage(new Page<>(page, size), wrapper);

        IPage<Map<String, Object>> result = new Page<>(page, size, pkgPage.getTotal());
        List<Map<String, Object>> list = new ArrayList<>();
        for (Package pkg : pkgPage.getRecords()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", pkg.getId());
            item.put("trackingNumber", pkg.getTrackingNumber());
            item.put("packageName", pkg.getPackageName());
            item.put("senderName", pkg.getSenderName());
            item.put("senderPhone", pkg.getSenderPhone());
            item.put("receiverName", pkg.getReceiverName());
            item.put("receiverPhone", pkg.getReceiverPhone());
            item.put("weight", pkg.getWeight());
            item.put("volume", pkg.getVolume());
            item.put("createdAt", pkg.getCreatedAt());

            InboundPackage inbound = inboundPackageMapper.selectOne(
                    new QueryWrapper<InboundPackage>().eq("package_id", pkg.getId()));
            if (inbound != null) {
                item.put("status", inbound.getStatus());
                item.put("cabinetType", inbound.getCabinetType());
                item.put("pickupCode", inbound.getPickupCode());
                item.put("enterTime", inbound.getEnterTime());
                item.put("outTime", inbound.getOutTime());
            } else {
                item.put("status", null);
                item.put("cabinetType", null);
                item.put("pickupCode", null);
                item.put("enterTime", null);
                item.put("outTime", null);
            }

            SendPackage sp = sendPackageMapper.selectOne(
                    new QueryWrapper<SendPackage>().eq("package_id", pkg.getId()));
            if (sp != null) {
                item.put("sendStatus", sp.getStatus());
            }

            list.add(item);
        }
        result.setRecords(list);
        return result;
    }
}
