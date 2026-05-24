package com.neu.webapp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neu.webapp.entity.User;
import com.neu.webapp.mapper.UserMapper;
import com.neu.webapp.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
