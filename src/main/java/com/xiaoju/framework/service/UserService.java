package com.xiaoju.framework.service;

import com.xiaoju.framework.entity.dto.User;
import com.xiaoju.framework.entity.dto.UserBaseInfo;
import com.xiaoju.framework.entity.request.auth.UserLoginReq;
import com.xiaoju.framework.entity.request.auth.UserRegisterReq;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Created by didi on 2021/4/22.
 */
public interface UserService {
    User register(UserRegisterReq req, HttpServletRequest request, HttpServletResponse response);
    User login(UserLoginReq req, HttpServletRequest request, HttpServletResponse response);
    Integer logout(HttpServletRequest request, HttpServletResponse response);
    User registerOrLogin(UserRegisterReq req, HttpServletRequest request, HttpServletResponse response);

    /**
     * 获取用户对应权限的路径匹配列表
     *
     * @param username 用户名称
     * @return 对应权限的路径匹配列表
     */
    List<String> getUserAuthorityContent(String username);

    /**
     * 获取所有注册用户
     */
    List<UserBaseInfo> getAllUser();

    void fixUserid();
}
