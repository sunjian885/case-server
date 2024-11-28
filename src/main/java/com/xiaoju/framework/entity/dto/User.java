package com.xiaoju.framework.entity.dto;

import lombok.Data;

import java.util.Date;

/**
 * Created by didi on 2021/4/22.
 */
@Data
public class User {

    private Long id;

    //用户的userid，在会员系统中的userid
    private Long userid;

    //用户真实姓名
    private String realName;

    //用户登录用的username，是手机号码
    private String username;

    //用户登录用的密码，也是手机号码
    private String password;

    private String salt;

    private String authorityName;

    private Integer isDelete;

    private Integer channel;

    private Long productLineId;

    private Date gmtCreated;

    private Date gmtUpdated;

    /**
     * 获取用户的三要素字符串
     * @param user
     * @return
     */
    public static String buildUserKey(User user) {
        StringBuilder builder = new StringBuilder();
        builder.append(user.getUsername()).append(",")
                .append(user.getChannel()).append(",")
                .append(user.getProductLineId());
        return builder.toString();
    }
}
