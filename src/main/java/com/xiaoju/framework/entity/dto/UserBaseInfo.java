package com.xiaoju.framework.entity.dto;

import lombok.Data;

@Data
public class UserBaseInfo {

    private Long id;

    //用户的userid，在会员系统中的userid
    private Long userid;

    //用户真实姓名
    private String realName;

    //用户登录用的username，是手机号码
    private String username;
}
