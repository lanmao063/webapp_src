package com.neu.webapp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neu.webapp.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    //查询主键，全查询，插入，更新，删除等基本操作由BaseMapper提供
}
