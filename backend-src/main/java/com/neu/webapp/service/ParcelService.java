package com.neu.webapp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.neu.webapp.dto.ParcelEntryRequest;
import com.neu.webapp.entity.Parcel;

public interface ParcelService extends IService<Parcel> {

    Parcel enterWarehouse(ParcelEntryRequest request, String enteredBy);

    void pickup(String id);

    IPage<Parcel> search(String keyword, String status, int page, int size);
}
