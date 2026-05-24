package com.neu.webapp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.neu.webapp.entity.InboundPackage;

import java.util.List;
import java.util.Map;

public interface InboundPackageService extends IService<InboundPackage> {

    String warehouseEntry(String trackingNumber, Long courierId);

    void checkout(String trackingNumber, Long userId);

    List<Map<String, Object>> myPickupCodes(Long userId);

    void authorizeProxy(Long id, Long userId, String proxyPhone);

    Map<String, Object> searchByTrackingNumber(String trackingNumber, Long userId);

    IPage<Map<String, Object>> searchAutoCheckout(String keyword, int page, int size);
}
