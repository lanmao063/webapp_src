package com.neu.webapp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neu.webapp.dto.ParcelEntryRequest;
import com.neu.webapp.entity.Parcel;
import com.neu.webapp.exception.BusinessException;
import com.neu.webapp.mapper.ParcelMapper;
import com.neu.webapp.service.ParcelService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class ParcelServiceImpl extends ServiceImpl<ParcelMapper, Parcel> implements ParcelService {

    @Override
    @Transactional
    public Parcel enterWarehouse(ParcelEntryRequest request, String enteredBy) {
        String id = generateParcelId(request.getVolume().doubleValue());
        Parcel parcel = new Parcel();
        parcel.setId(id);
        parcel.setPackageName(request.getPackageName());
        parcel.setWeight(request.getWeight());
        parcel.setVolume(request.getVolume());
        parcel.setDeclaredValue(request.getDeclaredValue());
        parcel.setSenderName(request.getSenderName());
        parcel.setReceiverName(request.getReceiverName());
        parcel.setReceiverPhone(request.getReceiverPhone());
        parcel.setReceiverAddress(request.getReceiverAddress());
        parcel.setNotes(request.getNotes());
        parcel.setStatus("IN_WAREHOUSE");
        parcel.setEnterTime(LocalDateTime.now());
        parcel.setEnteredBy(enteredBy);
        baseMapper.insert(parcel);
        return parcel;
    }

    @Override
    @Transactional
    public void pickup(String id) {
        Parcel parcel = baseMapper.selectById(id);
        if (parcel == null) {
            throw new BusinessException("包裹不存在");
        }
        if ("PICKED_UP".equals(parcel.getStatus())) {
            throw new BusinessException("该包裹已取件");
        }
        parcel.setStatus("PICKED_UP");
        parcel.setOutTime(LocalDateTime.now());
        baseMapper.updateById(parcel);
    }

    @Override
    public IPage<Parcel> search(String keyword, String status, int page, int size) {
        QueryWrapper<Parcel> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                    .like("id", keyword)
                    .or()
                    .like("package_name", keyword)
                    .or()
                    .like("receiver_name", keyword)
                    .or()
                    .like("receiver_phone", keyword)
            );
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("enter_time");
        return baseMapper.selectPage(new Page<>(page, size), wrapper);
    }

    private String generateParcelId(double volume) {
        LocalDate today = LocalDate.now();
        int dayOfWeek = today.getDayOfWeek().getValue(); // 1=Mon ... 7=Sun
        String cabinetType;
        int cabinetNum;

        if (volume < 12000) {
            cabinetType = "SMALL";
            cabinetNum = 1;
        } else if (volume > 125000) {
            cabinetType = "LARGE";
            cabinetNum = 101;
        } else {
            cabinetType = "MEDIUM";
            cabinetNum = 51;
        }

        Integer maxSeq = baseMapper.getMaxSequenceNo(dayOfWeek, cabinetType);
        int sequenceNo = (maxSeq == null) ? 1 : maxSeq + 1;

        // ID format: {dayOfWeek}-{cabinetNum}-{sequenceNo}
        String id = dayOfWeek + "-" + cabinetNum + "-" + String.format("%04d", sequenceNo);

        // Store generation info in the parcel will be set by caller
        return id;
    }
}
