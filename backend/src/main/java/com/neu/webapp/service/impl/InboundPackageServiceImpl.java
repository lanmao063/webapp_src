package com.neu.webapp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neu.webapp.entity.InboundPackage;
import com.neu.webapp.entity.Package;
import com.neu.webapp.entity.SystemUser;
import com.neu.webapp.exception.BusinessException;
import com.neu.webapp.mapper.InboundPackageMapper;
import com.neu.webapp.mapper.PackageMapper;
import com.neu.webapp.mapper.SystemUserMapper;
import com.neu.webapp.service.InboundPackageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class InboundPackageServiceImpl extends ServiceImpl<InboundPackageMapper, InboundPackage> implements InboundPackageService {

    private final PackageMapper packageMapper;
    private final SystemUserMapper systemUserMapper;

    public InboundPackageServiceImpl(PackageMapper packageMapper, SystemUserMapper systemUserMapper) {
        this.packageMapper = packageMapper;
        this.systemUserMapper = systemUserMapper;
    }

    @Override
    @Transactional
    public String warehouseEntry(String trackingNumber, Long courierId) {
        Package pkg = packageMapper.selectOne(
                new QueryWrapper<Package>().eq("tracking_number", trackingNumber));
        if (pkg == null) {
            throw new BusinessException("快递单号不存在，请先录入包裹信息");
        }
        InboundPackage inbound = baseMapper.selectOne(
                new QueryWrapper<InboundPackage>().eq("package_id", pkg.getId()));
        if (inbound == null) {
            throw new BusinessException("该包裹未预录入入库信息");
        }
        if ("IN_WAREHOUSE".equals(inbound.getStatus())) {
            throw new BusinessException("该包裹已入库，请勿重复操作");
        }

        // 根据体积确定柜型 → 生成取件码
        double volume = pkg.getVolume() != null ? pkg.getVolume() : 0;
        String cabinetType;
        int cabinetNum;

        if (volume < 12000) {
            cabinetType = "SMALL";
            cabinetNum = (int)(Math.random() * 50) + 1;   // 1-50
        } else if (volume > 125000) {
            cabinetType = "LARGE";
            cabinetNum = (int)(Math.random() * 50) + 101; // 101-150
        } else {
            cabinetType = "MEDIUM";
            cabinetNum = (int)(Math.random() * 50) + 51;  // 51-100
        }

        String pickupCode = String.format("%02d-%d-%04d",
                cabinetNum,
                (int)(Math.random() * 9) + 1,
                (int)(Math.random() * 9000) + 1000);

        inbound.setPickupCode(pickupCode);
        inbound.setCabinetType(cabinetType);
        inbound.setStatus("IN_WAREHOUSE");
        inbound.setEnteredBy(courierId);
        inbound.setEnterTime(LocalDateTime.now());
        baseMapper.updateById(inbound);
        return pickupCode;
    }

    @Override
    @Transactional
    public void checkout(String trackingNumber, Long userId) {
        Package pkg = packageMapper.selectOne(
                new QueryWrapper<Package>().eq("tracking_number", trackingNumber));
        if (pkg == null) {
            throw new BusinessException("包裹不存在");
        }
        InboundPackage inbound = baseMapper.selectOne(
                new QueryWrapper<InboundPackage>().eq("package_id", pkg.getId()));
        if (inbound == null) {
            throw new BusinessException("该包裹未入库");
        }
        if (!"IN_WAREHOUSE".equals(inbound.getStatus())) {
            if ("CHECKED_OUT".equals(inbound.getStatus())) {
                throw new BusinessException("该包裹已出库");
            }
            throw new BusinessException("该包裹未入库，请先完成入库操作");
        }

        SystemUser user = systemUserMapper.selectById(userId);
        if (user == null || user.getPhone() == null) {
            throw new BusinessException("用户信息异常，无法验证取件权限");
        }
        if (pkg.getReceiverPhone() == null) {
            throw new BusinessException("该包裹收件人信息缺失，无法验证取件权限");
        }
        String userPhone = user.getPhone();
        String receiverPhone = pkg.getReceiverPhone();
        String proxyPhone = inbound.getProxyPhone();
        if (!userPhone.equals(receiverPhone) && !userPhone.equals(proxyPhone)) {
            throw new BusinessException("该包裹收件人手机号与您的手机号不匹配，这不是您的包裹");
        }

        inbound.setStatus("CHECKED_OUT");
        inbound.setOutTime(LocalDateTime.now());
        baseMapper.updateById(inbound);
    }

    @Override
    public List<Map<String, Object>> myPickupCodes(Long userId) {
        SystemUser user = systemUserMapper.selectById(userId);
        if (user == null || user.getPhone() == null) {
            return new ArrayList<>();
        }
        String phone = user.getPhone();

        List<InboundPackage> inbounds = baseMapper.selectList(
                new QueryWrapper<InboundPackage>()
                        .eq("status", "IN_WAREHOUSE")
                        .isNotNull("pickup_code"));

        List<Map<String, Object>> result = new ArrayList<>();
        for (InboundPackage ib : inbounds) {
            Package pkg = packageMapper.selectById(ib.getPackageId());
            if (pkg == null) continue;
            // 匹配收件人手机号或代取人手机号
            if (!phone.equals(pkg.getReceiverPhone()) && !phone.equals(ib.getProxyPhone())) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", ib.getId());
            item.put("trackingNumber", pkg.getTrackingNumber());
            item.put("packageName", pkg.getPackageName());
            item.put("pickupCode", ib.getPickupCode());
            item.put("cabinetType", ib.getCabinetType());
            item.put("proxyPhone", ib.getProxyPhone());
            item.put("enterTime", ib.getEnterTime());
            result.add(item);
        }
        return result;
    }

    @Override
    public void authorizeProxy(Long id, Long userId, String proxyPhone) {
        InboundPackage inbound = baseMapper.selectById(id);
        if (inbound == null) {
            throw new BusinessException("包裹不存在");
        }
        if (!"IN_WAREHOUSE".equals(inbound.getStatus())) {
            throw new BusinessException("该包裹未入库，无法设置代取");
        }
        Package pkg = packageMapper.selectById(inbound.getPackageId());
        if (pkg == null) {
            throw new BusinessException("包裹信息异常");
        }
        SystemUser user = systemUserMapper.selectById(userId);
        if (user == null || user.getPhone() == null) {
            throw new BusinessException("用户信息异常");
        }
        if (!user.getPhone().equals(pkg.getReceiverPhone())) {
            throw new BusinessException("只有收件人本人可以设置代取");
        }
        if (proxyPhone == null || proxyPhone.isBlank()) {
            throw new BusinessException("代取人手机号不能为空");
        }
        if (proxyPhone.equals(pkg.getReceiverPhone())) {
            throw new BusinessException("代取人不能是收件人本人");
        }
        inbound.setProxyPhone(proxyPhone);
        baseMapper.updateById(inbound);
    }

    @Override
    public Map<String, Object> searchByTrackingNumber(String trackingNumber, Long userId) {
        Package pkg = packageMapper.selectOne(
                new QueryWrapper<Package>().eq("tracking_number", trackingNumber));
        if (pkg == null) {
            return null;
        }
        InboundPackage inbound = baseMapper.selectOne(
                new QueryWrapper<InboundPackage>().eq("package_id", pkg.getId()));
        if (inbound == null || !"IN_WAREHOUSE".equals(inbound.getStatus())) {
            return null;
        }

        SystemUser user = systemUserMapper.selectById(userId);
        String userPhone = user != null ? user.getPhone() : null;
        String receiverPhone = pkg.getReceiverPhone();
        String proxyPhone = inbound.getProxyPhone();
        boolean phoneMatch = userPhone != null &&
                (userPhone.equals(receiverPhone) || userPhone.equals(proxyPhone));

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", inbound.getId());
        item.put("trackingNumber", pkg.getTrackingNumber());
        item.put("packageName", pkg.getPackageName());
        item.put("receiverPhone", receiverPhone);
        item.put("cabinetType", inbound.getCabinetType());
        item.put("pickupCode", inbound.getPickupCode());
        item.put("enterTime", inbound.getEnterTime());
        item.put("phoneMatch", phoneMatch);
        return item;
    }

    @Override
    public IPage<Map<String, Object>> searchAutoCheckout(String keyword, int page, int size) {
        QueryWrapper<InboundPackage> wrapper = new QueryWrapper<>();
        wrapper.eq("is_auto_checkout", 1);

        if (keyword != null && !keyword.isEmpty()) {
            QueryWrapper<Package> pkgWrapper = new QueryWrapper<>();
            pkgWrapper.and(w -> w
                .like("tracking_number", keyword)
                .or().like("receiver_name", keyword)
                .or().like("receiver_phone", keyword));
            pkgWrapper.select("id");
            java.util.List<Object> pkgIds = packageMapper.selectObjs(pkgWrapper);
            if (!pkgIds.isEmpty()) {
                wrapper.in("package_id", pkgIds);
            } else {
                wrapper.eq("package_id", -1L);
            }
        }

        wrapper.orderByDesc("auto_checkout_time");
        IPage<InboundPackage> ibPage = baseMapper.selectPage(new Page<>(page, size), wrapper);

        IPage<Map<String, Object>> result = new Page<>(page, size, ibPage.getTotal());
        java.util.List<Map<String, Object>> list = new java.util.ArrayList<>();
        for (InboundPackage ib : ibPage.getRecords()) {
            Map<String, Object> item = new java.util.LinkedHashMap<>();
            item.put("id", ib.getId());
            item.put("pickupCode", ib.getPickupCode());
            item.put("enterTime", ib.getEnterTime());
            item.put("outTime", ib.getOutTime());
            item.put("autoCheckoutTime", ib.getAutoCheckoutTime());

            Package pkg = packageMapper.selectById(ib.getPackageId());
            if (pkg != null) {
                item.put("trackingNumber", pkg.getTrackingNumber());
                item.put("packageName", pkg.getPackageName());
                item.put("receiverName", pkg.getReceiverName());
                item.put("receiverPhone", pkg.getReceiverPhone());
            }
            list.add(item);
        }
        result.setRecords(list);
        return result;
    }
}
