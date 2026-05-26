package com.neu.webapp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neu.webapp.dto.SendPackageRequest;
import com.neu.webapp.entity.Package;
import com.neu.webapp.entity.SendPackage;
import com.neu.webapp.entity.SystemUser;
import com.neu.webapp.exception.BusinessException;
import com.neu.webapp.mapper.PackageMapper;
import com.neu.webapp.mapper.SendPackageMapper;
import com.neu.webapp.mapper.SystemUserMapper;
import com.neu.webapp.service.SendPackageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class SendPackageServiceImpl extends ServiceImpl<SendPackageMapper, SendPackage> implements SendPackageService {

    private final PackageMapper packageMapper;
    private final SystemUserMapper systemUserMapper;

    public SendPackageServiceImpl(PackageMapper packageMapper, SystemUserMapper systemUserMapper) {
        this.packageMapper = packageMapper;
        this.systemUserMapper = systemUserMapper;
    }

    @Override
    @Transactional
    public SendPackage createSendPackage(SendPackageRequest request, Long userId) {
        Package pkg = new Package();
        pkg.setTrackingNumber("SF" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
                //生成唯一的运单号，格式为SF+年月日时分秒+4位随机字符串（20位）
        pkg.setPackageName(request.getPackageName());
        pkg.setWeight(request.getWeight());
        pkg.setVolume(request.getVolume());
        pkg.setSenderName(request.getSenderName());
        pkg.setSenderPhone(request.getSenderPhone());
        pkg.setSenderAddress(request.getSenderAddress());
        pkg.setReceiverName(request.getReceiverName());
        pkg.setReceiverPhone(request.getReceiverPhone());
        pkg.setReceiverAddress(request.getReceiverAddress());
        pkg.setNotes(request.getNotes());
        packageMapper.insert(pkg);//将包裹基本信息保存到Package表中

        SendPackage sp = new SendPackage();
        sp.setPackageId(pkg.getId());
        sp.setPickupMethod(request.getPickupMethod());
        sp.setAppointmentTime(request.getAppointmentTime());
        sp.setCreatedBy(userId);
        sp.setStatus("SUBMITTED");
        sp.setIsPaid(0);
        sp.setFee(calculateFee(pkg.getWeight()));
        baseMapper.insert(sp);
        //将订单信息保存到SendPackage表中，初始状态为SUBMITTED，未支付

        //补全sendpackage中的包裹相关字段，方便后续前端查询展示
        populatePackageFields(sp, pkg);
        return sp;
    }

    @Override//管理员搜索订单接口，支持根据运单号、寄件人/收件人姓名、电话模糊搜索，并可筛选订单状态
    public IPage<SendPackage> search(String keyword, String status, int page, int size) {
        QueryWrapper<SendPackage> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            QueryWrapper<Package> pkgWrapper = new QueryWrapper<>();
            pkgWrapper.like("tracking_number", keyword)
                .or().like("receiver_name", keyword)
                .or().like("receiver_phone", keyword)
                .or().like("sender_name", keyword)
                .or().like("sender_phone", keyword);
            pkgWrapper.select("id");
            java.util.List<Object> pkgIds = packageMapper.selectObjs(pkgWrapper);
            if (!pkgIds.isEmpty()) {
                wrapper.in("package_id", pkgIds);
            } else {
                wrapper.eq("package_id", -1L);
            }
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("created_at");
        IPage<SendPackage> result = baseMapper.selectPage(new Page<>(page, size), wrapper);
        result.getRecords().forEach(this::fillPackageFields);
        return result;
    }

    @Override//获取待审核订单列表
    public IPage<SendPackage> pendingList(int page, int size) {
        QueryWrapper<SendPackage> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "SUBMITTED").orderByDesc("created_at");
        IPage<SendPackage> result = baseMapper.selectPage(new Page<>(page, size), wrapper);
        result.getRecords().forEach(this::fillPackageFields);
        return result;
    }

    @Override
    @Transactional//审批订单接口，管理员可以审批通过或驳回订单，审批通过后自动分配快递员
    public void approve(Long id, SendPackage updateData, Long managerId) {
        SendPackage sp = baseMapper.selectById(id);
        if (sp == null) {
            throw new BusinessException("订单不存在");
        }
        if (!"SUBMITTED".equals(sp.getStatus())) {
            throw new BusinessException("订单状态不是待审核");
        }
        if (updateData.getFee() != null) {
            sp.setFee(updateData.getFee());
        }
        if (updateData.getPickupMethod() != null) {
            sp.setPickupMethod(updateData.getPickupMethod());
        }
        if (updateData.getAppointmentTime() != null) {
            sp.setAppointmentTime(updateData.getAppointmentTime());
        }
        sp.setStatus("APPROVED");
        sp.setApprovedAt(LocalDateTime.now());
        // 自动分配快递员
        java.util.List<SystemUser> couriers = systemUserMapper.selectList(
                new QueryWrapper<SystemUser>().eq("role", "COURIER").eq("status", 1));
        if (!couriers.isEmpty()) {
            int index = (int)(Math.random() * couriers.size());
            sp.setCourierId(couriers.get(index).getId());
        }
        baseMapper.updateById(sp);
    }

    @Override
    @Transactional//驳回订单接口，管理员可以驳回待审核的订单
    public void reject(Long id, Long managerId) {
        SendPackage sp = baseMapper.selectById(id);
        if (sp == null) {
            throw new BusinessException("订单不存在");
        }
        if (!"SUBMITTED".equals(sp.getStatus())) {
            throw new BusinessException("订单状态不是待审核");
        }
        sp.setStatus("REJECTED");
        baseMapper.updateById(sp);
    }

    @Override//用户未付款查找
    public IPage<SendPackage> myUnpaid(Long userId, int page, int size) {
        QueryWrapper<SendPackage> wrapper = new QueryWrapper<>();
        wrapper.eq("created_by", userId).eq("status", "APPROVED").orderByDesc("created_at");
        IPage<SendPackage> result = baseMapper.selectPage(new Page<>(page, size), wrapper);
        result.getRecords().forEach(this::fillPackageFields);
        return result;
    }

    @Override
    @Transactional//用户支付
    public void pay(Long id, Long userId) {
        SendPackage sp = baseMapper.selectById(id);
        if (sp == null) {
            throw new BusinessException("订单不存在");
        }
        if (!"APPROVED".equals(sp.getStatus())) {
            throw new BusinessException("订单状态不是待付款");
        }
        if (!sp.getCreatedBy().equals(userId)) {
            throw new BusinessException("只能支付自己的订单");
        }
        sp.setStatus("PAID");
        sp.setIsPaid(1);
        sp.setPaidAt(LocalDateTime.now());
        baseMapper.updateById(sp);
    }

    @Override//用于被选中的快递员查看已支付订单列表，方便揽收
    public IPage<SendPackage> paidList(Long courierId, int page, int size) {
        QueryWrapper<SendPackage> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "PAID").eq("courier_id", courierId).orderByDesc("created_at");
        IPage<SendPackage> result = baseMapper.selectPage(new Page<>(page, size), wrapper);
        result.getRecords().forEach(this::fillPackageFields);
        return result;
    }

    @Override
    @Transactional//快递员揽收功能
    public void collect(Long id, Long courierId) {
        SendPackage sp = baseMapper.selectById(id);
        if (sp == null) {
            throw new BusinessException("订单不存在");
        }
        if (!"PAID".equals(sp.getStatus())) {
            throw new BusinessException("订单状态不是待揽收");
        }
        if (sp.getCourierId() == null || !sp.getCourierId().equals(courierId)) {
            throw new BusinessException("该订单未分配给您，无法揽收");
        }
        sp.setStatus("COLLECTED");
        sp.setCollectedAt(LocalDateTime.now());
        baseMapper.updateById(sp);
    }

    private void fillPackageFields(SendPackage sp) {
        // 通过 package_id 去 package 表查出完整数据，把字段拷贝到 SendPackage 对象上返回给前端。
        if (sp.getPackageId() != null) {
            Package pkg = packageMapper.selectById(sp.getPackageId());
            if (pkg != null) {
                populatePackageFields(sp, pkg);
            }
        }
    }

    private void populatePackageFields(SendPackage sp, Package pkg) {
        sp.setTrackingNumber(pkg.getTrackingNumber());
        sp.setPackageName(pkg.getPackageName());
        sp.setWeight(pkg.getWeight());
        sp.setSenderName(pkg.getSenderName());
        sp.setSenderPhone(pkg.getSenderPhone());
        sp.setSenderAddress(pkg.getSenderAddress());
        sp.setReceiverName(pkg.getReceiverName());
        sp.setReceiverPhone(pkg.getReceiverPhone());
        sp.setReceiverAddress(pkg.getReceiverAddress());
        sp.setNotes(pkg.getNotes());
    }

    private double calculateFee(Double weight) {
        if (weight == null || weight <= 0) return 10.0;
        if (weight <= 1) return 10.0;
        if (weight <= 5) return 15.0;
        if (weight <= 10) return 25.0;
        if (weight <= 20) return 40.0;
        return 60.0 + (weight - 20) * 3.0;
    }
}
