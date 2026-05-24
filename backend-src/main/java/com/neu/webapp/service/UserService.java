package com.neu.webapp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.neu.webapp.entity.User;

public interface UserService extends IService<User> {
    //继承IService接口，提供User实体的CRUD操作
}
