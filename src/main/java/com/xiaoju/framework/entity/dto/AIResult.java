package com.xiaoju.framework.entity.dto;

import lombok.Data;

import java.util.Date;

@Data
public class AIResult {

    Long id;
    Long userid;
    String username;
    String prompt;
    String token;
    String result;
    String type;
    int status;
    Date gmtCreated;
    Date gmtUpdated;
}
