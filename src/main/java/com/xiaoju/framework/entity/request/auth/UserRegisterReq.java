package com.xiaoju.framework.entity.request.auth;

import lombok.Data;

/**
 * Created by didi on 2021/4/22.
 */
@Data
public class UserRegisterReq {
    private String username;

    private String password;

    //用户的userid，在会员系统中的userid
    private Long userid;

    //用户真实姓名
    private String realName;
}
