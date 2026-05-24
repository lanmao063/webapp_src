package com.neu.webapp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neu.webapp.dto.ForgotPasswordRequest;
import com.neu.webapp.dto.LoginRequest;
import com.neu.webapp.dto.RegisterRequest;
import com.neu.webapp.entity.SystemUser;
import com.neu.webapp.exception.BusinessException;
import com.neu.webapp.mapper.SystemUserMapper;
import com.neu.webapp.security.JwtUtil;
import com.neu.webapp.service.SystemUserService;
import com.neu.webapp.vo.LoginResponse;
import com.neu.webapp.vo.UserInfo;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SystemUserServiceImpl extends ServiceImpl<SystemUserMapper, SystemUser> implements SystemUserService {

    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public SystemUserServiceImpl(BCryptPasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        SystemUser user = baseMapper.selectOne(
                new QueryWrapper<SystemUser>().eq("username", request.getUsername())
        );
        if (user == null) {
            throw new BusinessException("用户不存在，请先注册");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole(), request.isRememberMe());
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getRole());
    }

    @Override
    public void register(RegisterRequest request) {
        SystemUser existing = baseMapper.selectOne(
                new QueryWrapper<SystemUser>().eq("username", request.getUsername())
        );
        if (existing != null) {
            throw new BusinessException("用户名已存在");
        }
        SystemUser user = new SystemUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setPhone(request.getPhone());
        user.setIdNumber(request.getIdNumber());
        user.setStatus(1);
        baseMapper.insert(user);
    }

    @Override
    public void resetPassword(ForgotPasswordRequest request) {
        SystemUser user = baseMapper.selectOne(
                new QueryWrapper<SystemUser>()
                        .eq("username", request.getUsername())
                        .eq("phone", request.getPhone())
        );
        if (user == null) {
            throw new BusinessException("用户名与电话不匹配");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        baseMapper.updateById(user);
    }

    @Override
    public UserInfo getCurrentUserInfo(Long userId) {
        SystemUser user = baseMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        UserInfo info = new UserInfo();
        info.setId(user.getId());
        info.setUsername(user.getUsername());
        info.setRole(user.getRole());
        info.setRealName(user.getRealName());
        info.setPhone(user.getPhone());
        info.setIdNumber(user.getIdNumber());
        return info;
    }
}
