package com.neu.webapp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.neu.webapp.dto.ForgotPasswordRequest;
import com.neu.webapp.dto.LoginRequest;
import com.neu.webapp.dto.RegisterRequest;
import com.neu.webapp.entity.SystemUser;
import com.neu.webapp.vo.LoginResponse;
import com.neu.webapp.vo.UserInfo;

public interface SystemUserService extends IService<SystemUser> {

    LoginResponse login(LoginRequest request);

    void register(RegisterRequest request);

    void resetPassword(ForgotPasswordRequest request);

    UserInfo getCurrentUserInfo(Long userId);
}
