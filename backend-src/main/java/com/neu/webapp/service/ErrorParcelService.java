package com.neu.webapp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.neu.webapp.entity.ErrorParcel;

public interface ErrorParcelService extends IService<ErrorParcel> {

    ErrorParcel registerError(String parcelId, String errorType, String description, String reportedBy);

    void handleError(Long id, String handlerName, String handleResult);

    IPage<ErrorParcel> search(String status, int page, int size);
}
