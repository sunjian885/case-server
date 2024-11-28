package com.xiaoju.framework.service.DTO;

import lombok.Data;

import java.util.Date;

@Data
public class AIResultDTO {

    String callId;
    Boolean success;
    String result;
    String errorCode;
    String errorMsg;
    String token;
    String status;
    Date startTime;
    Date endTime;
}


