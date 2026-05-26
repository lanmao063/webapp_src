package com.neu.webapp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neu.webapp.entity.ErrorParcel;
import com.neu.webapp.entity.Package;
import com.neu.webapp.entity.SystemUser;
import com.neu.webapp.exception.BusinessException;
import com.neu.webapp.mapper.ErrorParcelMapper;
import com.neu.webapp.mapper.PackageMapper;
import com.neu.webapp.mapper.SystemUserMapper;
import com.neu.webapp.service.ErrorParcelService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ErrorParcelServiceImpl extends ServiceImpl<ErrorParcelMapper, ErrorParcel> implements ErrorParcelService {

    private final PackageMapper packageMapper;
    private final SystemUserMapper systemUserMapper;

    public ErrorParcelServiceImpl(PackageMapper packageMapper, SystemUserMapper systemUserMapper) {
        this.packageMapper = packageMapper;
        this.systemUserMapper = systemUserMapper;
    }//注入

    @Override
    @Transactional//登记异常
    public ErrorParcel registerError(String trackingNumber, String errorType, String description, String reportedBy) {
        Package pkg = packageMapper.selectOne(
                new QueryWrapper<Package>().eq("tracking_number", trackingNumber));
        if (pkg == null) {
            throw new BusinessException("快递单号不存在");
        }
        ErrorParcel error = new ErrorParcel();
        error.setPackageId(pkg.getId());
        error.setErrorType(errorType);
        error.setDescription(description);
        error.setReportedBy(reportedBy);
        error.setStatus("UNRESOLVED");
        baseMapper.insert(error);
        return error;
    }

    @Override
    @Transactional//管理员处理异常
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

    @Override//管理员查询异常记录
    public IPage<ErrorParcel> search(String status, int page, int size) {
        QueryWrapper<ErrorParcel> wrapper = new QueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("created_at");
        IPage<ErrorParcel> result = baseMapper.selectPage(new Page<>(page, size), wrapper);
        for (ErrorParcel ep : result.getRecords()) {
            if (ep.getPackageId() != null) {
                Package pkg = packageMapper.selectById(ep.getPackageId());
                if (pkg != null) {
                    ep.setTrackingNumber(pkg.getTrackingNumber());
                }
            }
            if (ep.getReportedBy() != null) {
                SystemUser reporter = systemUserMapper.selectOne(
                        new QueryWrapper<SystemUser>().eq("username", ep.getReportedBy()));
                if (reporter != null) {
                    ep.setReporterPhone(reporter.getPhone());
                }
            }
        }
        return result;
    }

    @Override//用户查询自己提交的异常记录
    public IPage<ErrorParcel> myErrors(String username, int page, int size) {
        QueryWrapper<ErrorParcel> wrapper = new QueryWrapper<>();
        wrapper.eq("reported_by", username).orderByDesc("created_at");
        IPage<ErrorParcel> result = baseMapper.selectPage(new Page<>(page, size), wrapper);
        for (ErrorParcel ep : result.getRecords()) {
            if (ep.getPackageId() != null) {
                Package pkg = packageMapper.selectById(ep.getPackageId());
                if (pkg != null) {
                    ep.setTrackingNumber(pkg.getTrackingNumber());
                }
            }
        }
        return result;
    }
}
