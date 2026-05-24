package com.neu.webapp.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.neu.webapp.common.Result;
import com.neu.webapp.security.UserContext;
import com.neu.webapp.service.InboundPackageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/inbound")
public class InboundPackageController {

    private final InboundPackageService inboundPackageService;

    public InboundPackageController(InboundPackageService inboundPackageService) {
        this.inboundPackageService = inboundPackageService;
    }

    @PutMapping("/{trackingNumber}/warehouse-entry")
    public Result<String> warehouseEntry(@PathVariable String trackingNumber) {
        String pickupCode = inboundPackageService.warehouseEntry(trackingNumber, UserContext.getCurrentUserId());
        return Result.ok(pickupCode);
    }

    @PutMapping("/{trackingNumber}/checkout")
    public Result<Void> checkout(@PathVariable String trackingNumber) {
        inboundPackageService.checkout(trackingNumber, UserContext.getCurrentUserId());
        return Result.ok();
    }

    @GetMapping("/search")
    public Result<Map<String, Object>> search(@RequestParam String trackingNumber) {
        Map<String, Object> data = inboundPackageService.searchByTrackingNumber(
                trackingNumber, UserContext.getCurrentUserId());
        if (data == null) {
            return Result.fail(404, "未找到该包裹或包裹不在库");
        }
        return Result.ok(data);
    }

    @GetMapping("/my-pickup-codes")
    public Result<List<Map<String, Object>>> myPickupCodes() {
        return Result.ok(inboundPackageService.myPickupCodes(UserContext.getCurrentUserId()));
    }

    @PutMapping("/{id}/authorize-proxy")
    public Result<Void> authorizeProxy(@PathVariable Long id, @RequestBody Map<String, String> body) {
        inboundPackageService.authorizeProxy(id, UserContext.getCurrentUserId(), body.get("proxyPhone"));
        return Result.ok();
    }

    @GetMapping("/auto-checkout-list")
    public Result<IPage<Map<String, Object>>> autoCheckoutList(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(inboundPackageService.searchAutoCheckout(keyword, page, size));
    }
}
