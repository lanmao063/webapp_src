package com.neu.webapp.controller;

import com.neu.webapp.common.Result;
import com.neu.webapp.dto.ForgotPasswordRequest;
import com.neu.webapp.dto.LoginRequest;
import com.neu.webapp.dto.RegisterRequest;
import com.neu.webapp.security.UserContext;
import com.neu.webapp.service.SystemUserService;
import com.neu.webapp.vo.LoginResponse;
import com.neu.webapp.vo.UserInfo;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final SystemUserService systemUserService;

    public AuthController(SystemUserService systemUserService) {
        this.systemUserService = systemUserService;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(systemUserService.login(request));
    }

    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterRequest request) {
        systemUserService.register(request);
        return Result.ok();
    }

    @PostMapping("/forgot-password")
    public Result<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        systemUserService.resetPassword(request);
        return Result.ok();
    }

    @PostMapping("/logout")
    public Result<?> logout() {
        return Result.ok();
    }

    @GetMapping("/current")
    public Result<UserInfo> current() {
        Long userId = UserContext.getCurrentUserId();
        return Result.ok(systemUserService.getCurrentUserInfo(userId));
    }
}
