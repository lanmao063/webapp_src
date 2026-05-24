package com.neu.webapp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neu.webapp.entity.SendPackage;
import com.neu.webapp.entity.SystemUser;
import com.neu.webapp.exception.BusinessException;
import com.neu.webapp.mapper.SendPackageMapper;
import com.neu.webapp.mapper.SystemUserMapper;
import com.neu.webapp.service.SendPackageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SendPackageServiceImpl extends ServiceImpl<SendPackageMapper, SendPackage> implements SendPackageService {

    private final SystemUserMapper systemUserMapper;

    public SendPackageServiceImpl(SystemUserMapper systemUserMapper) {
        this.systemUserMapper = systemUserMapper;
    }

    @Override
    public SendPackage createSendPackage(SendPackage sendPackage, Long userId) {
        String trackNo = "SP" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        sendPackage.setTrackingNumber(trackNo);
        sendPackage.setCreatedBy(userId);
        sendPackage.setStatus("PENDING");
        sendPackage.setIsPaid(0);
        sendPackage.setFee(calculateFee(sendPackage.getWeight()));
        baseMapper.insert(sendPackage);
        return sendPackage;
    }

    @Override
    public IPage<SendPackage> search(String keyword, String status, int page, int size) {
        QueryWrapper<SendPackage> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                    .like("tracking_number", keyword)
                    .or()
                    .like("sender_name", keyword)
                    .or()
                    .like("receiver_name", keyword)
                    .or()
                    .like("sender_phone", keyword)
                    .or()
                    .like("receiver_phone", keyword)
            );
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("created_at");
        return baseMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public IPage<SendPackage> pendingList(int page, int size) {
        QueryWrapper<SendPackage> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "PENDING").orderByDesc("created_at");
        return baseMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    @Transactional
    public void approve(Long id, SendPackage updateData, Long managerId) {
        SendPackage sp = baseMapper.selectById(id);
        if (sp == null) {
            throw new BusinessException("订单不存在");
        }
        if (!"PENDING".equals(sp.getStatus())) {
            throw new BusinessException("订单状态不是待审核");
        }
        if (updateData.getWeight() != null) {
            sp.setWeight(updateData.getWeight());
            sp.setFee(calculateFee(updateData.getWeight()));
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
        sp.setStatus("UNPAID");
        baseMapper.updateById(sp);
    }

    @Override
    public void reject(Long id, Long managerId) {
        SendPackage sp = baseMapper.selectById(id);
        if (sp == null) {
            throw new BusinessException("订单不存在");
        }
        if (!"PENDING".equals(sp.getStatus())) {
            throw new BusinessException("订单状态不是待审核");
        }
        sp.setStatus("REJECTED");
        baseMapper.updateById(sp);
    }

    @Override
    public IPage<SendPackage> myUnpaid(Long userId, int page, int size) {
        QueryWrapper<SendPackage> wrapper = new QueryWrapper<>();
        wrapper.eq("created_by", userId).eq("status", "UNPAID").orderByDesc("created_at");
        return baseMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    @Transactional
    public void pay(Long id, Long userId) {
        SendPackage sp = baseMapper.selectById(id);
        if (sp == null) {
            throw new BusinessException("订单不存在");
        }
        if (!"UNPAID".equals(sp.getStatus())) {
            throw new BusinessException("订单状态不是待付款");
        }
        if (!sp.getCreatedBy().equals(userId)) {
            throw new BusinessException("只能支付自己的订单");
        }
        sp.setStatus("PAID");
        sp.setIsPaid(1);
        baseMapper.updateById(sp);
    }

    @Override
    public IPage<SendPackage> paidList(int page, int size) {
        QueryWrapper<SendPackage> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "PAID").orderByDesc("created_at");
        return baseMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public void pickup(Long id, Long courierId) {
        SendPackage sp = baseMapper.selectById(id);
        if (sp == null) {
            throw new BusinessException("订单不存在");
        }
        if (!"PAID".equals(sp.getStatus())) {
            throw new BusinessException("订单状态不是已付款");
        }
        sp.setStatus("DELIVERING");
        baseMapper.updateById(sp);
    }

    @Override
    public void deliver(Long id, Long courierId) {
        SendPackage sp = baseMapper.selectById(id);
        if (sp == null) {
            throw new BusinessException("订单不存在");
        }
        if (!"DELIVERING".equals(sp.getStatus())) {
            throw new BusinessException("订单状态不是派送中");
        }
        sp.setStatus("IN_WAREHOUSE");
        // 生成取件码: 柜号-排号-序号
        String code = String.format("%02d-%d-%04d",
                (int)(Math.random() * 99) + 1,
                (int)(Math.random() * 9) + 1,
                (int)(Math.random() * 9000) + 1000);
        sp.setPickupCode(code);
        baseMapper.updateById(sp);
    }

    @Override
    public List<SendPackage> myPickupCodes(Long userId) {
        SystemUser user = systemUserMapper.selectById(userId);
        if (user == null || user.getPhone() == null) {
            return new ArrayList<>();
        }
        QueryWrapper<SendPackage> wrapper = new QueryWrapper<>();
        wrapper.eq("receiver_phone", user.getPhone())
                .eq("status", "IN_WAREHOUSE")
                .isNotNull("pickup_code")
                .orderByDesc("created_at");
        return baseMapper.selectList(wrapper);
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
