package com.neu.webapp.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.neu.webapp.common.Result;
import com.neu.webapp.entity.SendPackage;
import com.neu.webapp.security.UserContext;
import com.neu.webapp.service.SendPackageService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/send-package")
public class SendPackageController {

    private final SendPackageService sendPackageService;

    public SendPackageController(SendPackageService sendPackageService) {
        this.sendPackageService = sendPackageService;
    }

    @PostMapping
    public Result<SendPackage> create(@RequestBody SendPackage sendPackage) {
        Long userId = UserContext.getCurrentUserId();
        return Result.ok(sendPackageService.createSendPackage(sendPackage, userId));
    }

    @GetMapping("/{trackingNumber}")
    public Result<SendPackage> getByTracking(@PathVariable String trackingNumber) {
        SendPackage sp = sendPackageService.getOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SendPackage>()
                        .eq("tracking_number", trackingNumber)
        );
        if (sp == null) {
            return Result.fail(404, "寄件订单不存在");
        }
        return Result.ok(sp);
    }

    @GetMapping("/search")
    public Result<IPage<SendPackage>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(sendPackageService.search(keyword, status, page, size));
    }

    @GetMapping("/pending-list")
    public Result<IPage<SendPackage>> pendingList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(sendPackageService.pendingList(page, size));
    }

    @PutMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id, @RequestBody SendPackage updateData) {
        sendPackageService.approve(id, updateData, UserContext.getCurrentUserId());
        return Result.ok();
    }

    @PutMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id) {
        sendPackageService.reject(id, UserContext.getCurrentUserId());
        return Result.ok();
    }

    @GetMapping("/my-unpaid")
    public Result<IPage<SendPackage>> myUnpaid(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(sendPackageService.myUnpaid(UserContext.getCurrentUserId(), page, size));
    }

    @PutMapping("/{id}/pay")
    public Result<Void> pay(@PathVariable Long id) {
        sendPackageService.pay(id, UserContext.getCurrentUserId());
        return Result.ok();
    }

    @GetMapping("/paid-list")
    public Result<IPage<SendPackage>> paidList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(sendPackageService.paidList(page, size));
    }

    @PutMapping("/{id}/pickup")
    public Result<Void> pickup(@PathVariable Long id) {
        sendPackageService.pickup(id, UserContext.getCurrentUserId());
        return Result.ok();
    }

    @PutMapping("/{id}/deliver")
    public Result<String> deliver(@PathVariable Long id) {
        sendPackageService.deliver(id, UserContext.getCurrentUserId());
        SendPackage sp = sendPackageService.getById(id);
        return Result.ok(sp.getPickupCode());
    }

    @GetMapping("/my-pickup-codes")
    public Result<java.util.List<SendPackage>> myPickupCodes() {
        return Result.ok(sendPackageService.myPickupCodes(UserContext.getCurrentUserId()));
    }
}
