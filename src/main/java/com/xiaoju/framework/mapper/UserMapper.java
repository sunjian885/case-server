package com.xiaoju.framework.mapper;

import com.xiaoju.framework.entity.dto.User;

import java.util.List;

public interface UserMapper {
    int deleteByPrimaryKey(Long id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Long id);
    User selectByUserid(Long Userid);

    User selectByUserName(String username);
    User selectByRealName(String realname);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    List<User> getAllUser();
}