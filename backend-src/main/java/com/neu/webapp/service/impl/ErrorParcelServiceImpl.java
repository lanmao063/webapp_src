package com.neu.webapp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neu.webapp.entity.ErrorParcel;
import com.neu.webapp.entity.Parcel;
import com.neu.webapp.exception.BusinessException;
import com.neu.webapp.mapper.ErrorParcelMapper;
import com.neu.webapp.service.ErrorParcelService;
import com.neu.webapp.service.ParcelService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ErrorParcelServiceImpl extends ServiceImpl<ErrorParcelMapper, ErrorParcel> implements ErrorParcelService {

    private final ParcelService parcelService;

    public ErrorParcelServiceImpl(ParcelService parcelService) {
        this.parcelService = parcelService;
    }

    @Override
    @Transactional
    public ErrorParcel registerError(String parcelId, String errorType, String description, String reportedBy) {
        Parcel parcel = parcelService.getById(parcelId);
        if (parcel == null) {
            throw new BusinessException("包裹不存在");
        }
        ErrorParcel error = new ErrorParcel();
        error.setParcelId(parcelId);
        error.setErrorType(errorType);
        error.setDescription(description);
        error.setReportedBy(reportedBy);
        error.setStatus("UNRESOLVED");
        baseMapper.insert(error);
        // Update parcel status
        parcel.setStatus("ERROR");
        parcelService.updateById(parcel);
        return error;
    }

    @Override
    @Transactional
    public void handleError(Long id, String handlerName, String handleResult) {
        ErrorParcel error = baseMapper.selectById(id);
        if (error == null) {
            throw new BusinessException("异常记录不存在");
        }
        error.setHandlerName(handlerName);
        error.setHandleResult(handleResult);
        error.setHandleTime(LocalDateTime.now());
        error.setStatus("RESOLVED");
        baseMapper.updateById(error);
    }

    @Override
    public IPage<ErrorParcel> search(String status, int page, int size) {
        QueryWrapper<ErrorParcel> wrapper = new QueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("created_at");
        return baseMapper.selectPage(new Page<>(page, size), wrapper);
    }
}
