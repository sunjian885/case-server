package com.xiaoju.framework.entity.dto;

import lombok.Data;

import java.util.Date;

@Data
public class Config {

    Long id;
    String type;
    String key;
    String prompt;
    String value;
    int status;
    Date gmtUpdated;
    Date gmtCreated;

}
