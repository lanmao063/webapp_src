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
    }//注入

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
        }//校验包裹状态

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
                (int)(Math.random() * 5) + 1,//1-5
                (int)(Math.random() * 9000) + 1000);

        inbound.setPickupCode(pickupCode);
        inbound.setCabinetType(cabinetType);
        inbound.setStatus("IN_WAREHOUSE");
        inbound.setEnteredBy(courierId);
        inbound.setEnterTime(LocalDateTime.now());
        baseMapper.updateById(inbound);//更新入库信息
        return pickupCode;
    }

    @Override
    @Transactional//出库
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
        }//校验包裹状态

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
        }//校验取件人的权限

        inbound.setStatus("CHECKED_OUT");
        inbound.setOutTime(LocalDateTime.now());
        baseMapper.updateById(inbound);//更新包裹状态
    }

    @Override//查询我的取件码列表
    public List<Map<String, Object>> myPickupCodes(Long userId) {
        SystemUser user = systemUserMapper.selectById(userId);
        if (user == null || user.getPhone() == null) {
            return new ArrayList<>();
        }
        String phone = user.getPhone();

        List<InboundPackage> inbounds = baseMapper.selectList(
                new QueryWrapper<InboundPackage>()
                        .eq("status", "IN_WAREHOUSE")
                        .isNotNull("pickup_code"));//只查询在库且已生成取件码的所有包裹

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
        }//将符合条件的包裹信息组装成列表返回
        return result;
    }

    @Override//设置代取人
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

    @Override//用户查询包裹信息
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

    @Override//用户凭运单号+手机号查询已入库包裹的取件信息
    public Map<String, Object> publicSearch(String trackingNumber, String phone) {
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
        if (phone == null || phone.isBlank()) {
            throw new BusinessException("请输入手机号");
        }
        String receiverPhone = pkg.getReceiverPhone();
        String proxyPhone = inbound.getProxyPhone();
        if (!phone.equals(receiverPhone) && !phone.equals(proxyPhone)) {
            throw new BusinessException("手机号与收件人或代取人不匹配");
        }
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", inbound.getId());
        item.put("trackingNumber", pkg.getTrackingNumber());
        item.put("packageName", pkg.getPackageName());
        item.put("receiverPhone", receiverPhone);
        item.put("cabinetType", inbound.getCabinetType());
        item.put("pickupCode", inbound.getPickupCode());
        item.put("enterTime", inbound.getEnterTime());
        return item;
    }

    @Override
    @Transactional//出库功能，但不要求用户登录，只要输入手机号和单号即可验证权限并完成出库
    public Map<String, Object> publicCheckout(String trackingNumber, String phone) {
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
        if (phone == null || phone.isBlank()) {
            throw new BusinessException("请输入手机号");
        }
        String receiverPhone = pkg.getReceiverPhone();
        if (receiverPhone == null) {
            throw new BusinessException("该包裹收件人信息缺失，无法验证取件权限");
        }
        String proxyPhone = inbound.getProxyPhone();
        if (!phone.equals(receiverPhone) && !phone.equals(proxyPhone)) {
            throw new BusinessException("该包裹收件人手机号与输入的手机号不匹配，这不是您的包裹");
        }

        inbound.setStatus("CHECKED_OUT");
        inbound.setOutTime(LocalDateTime.now());
        baseMapper.updateById(inbound);

        // 查询同一收件人仍在库的包裹
        List<InboundPackage> remainingInbounds = baseMapper.selectList(
                new QueryWrapper<InboundPackage>().eq("status", "IN_WAREHOUSE"));
        List<Map<String, Object>> remainingPackages = new ArrayList<>();
        for (InboundPackage ib : remainingInbounds) {
            if (ib.getId().equals(inbound.getId())) continue;
            Package rp = packageMapper.selectById(ib.getPackageId());
            if (rp != null && receiverPhone.equals(rp.getReceiverPhone())) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("trackingNumber", rp.getTrackingNumber());
                item.put("pickupCode", ib.getPickupCode());
                remainingPackages.add(item);
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("trackingNumber", pkg.getTrackingNumber());
        result.put("packageName", pkg.getPackageName());
        result.put("pickupCode", inbound.getPickupCode());
        result.put("cabinetType", inbound.getCabinetType());
        result.put("remainingCount", remainingPackages.size());
        result.put("remainingPackages", remainingPackages);
        return result;
    }

    @Override//管理员查询自动出库的包裹列表，支持根据快递单号、收件人姓名、收件人手机号模糊搜索
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
