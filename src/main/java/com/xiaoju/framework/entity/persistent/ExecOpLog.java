package com.xiaoju.framework.entity.persistent;

import lombok.Data;

import java.util.Date;

@Data
public class ExecOpLog {

    private Long id;
    private Long caseId;
    private Long recordId;
    private String username;
    private Long userid;
    private String caseContent;
    private Date gmtCreated;
    private Date gmtModified;
}
