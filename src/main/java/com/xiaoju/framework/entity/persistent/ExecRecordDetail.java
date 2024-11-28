package com.xiaoju.framework.entity.persistent;

import lombok.Data;

import java.util.Date;

@Data
public class ExecRecordDetail {

    private Long id;

    private Long caseId;

    private Long recordId;

    private String username;

    private Long userid;

    private Integer env;

    /**
     * 执行任务的执行记录
     */
    private String caseContent;

    /**
     * 是否删除执行记录，0 是未删除，1 是已删除
     */
    private Integer isDelete;

    /**
     * 用例执行数
     */
    private Integer execCount;
    /**
     * 用例通过数
     */
    private Integer successCount;

    /**
     * 用例忽略数 -- 不需要执行 -- 也不计算在内
     */
    private Integer ignoreCount;

    /**
     * 用例阻塞数
     */
    private Integer blockCount;

    /**
     * 用例失败数
     */
    private Integer failCount;

    private Date gmtCreated;

    private Date gmtModified;

    private Integer version;




}
