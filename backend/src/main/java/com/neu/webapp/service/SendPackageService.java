package com.neu.webapp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.neu.webapp.dto.SendPackageRequest;
import com.neu.webapp.entity.SendPackage;

public interface SendPackageService extends IService<SendPackage> {

    SendPackage createSendPackage(SendPackageRequest request, Long userId);

    IPage<SendPackage> search(String keyword, String status, int page, int size);

    IPage<SendPackage> pendingList(int page, int size);

    void approve(Long id, SendPackage updateData, Long managerId);

    void reject(Long id, Long managerId);

    IPage<SendPackage> myUnpaid(Long userId, int page, int size);

    void pay(Long id, Long userId);

    IPage<SendPackage> paidList(Long courierId, int page, int size);

    void collect(Long id, Long courierId);
}
