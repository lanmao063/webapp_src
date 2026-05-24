package com.neu.webapp.controller;

import com.neu.webapp.common.Result;
import com.neu.webapp.service.AdminService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Value("${admin.passcode}")
    private String adminPasscode;

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/package-init")
    public Result<Map<String, Object>> packageInit(@RequestBody Map<String, Object> body) {
        String passcode = (String) body.get("passcode");
        if (passcode == null || !passcode.equals(adminPasscode)) {
            return Result.fail(403, "通行码错误");
        }
        return Result.ok(adminService.packageInit(body));
    }
}
