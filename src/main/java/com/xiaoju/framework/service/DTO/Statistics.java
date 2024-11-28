package com.xiaoju.framework.service.DTO;

import lombok.Data;

@Data
public class Statistics {

    private Long userid;
    private String username;

    /**
     * 编写用例总数
     */
    private Integer amount;

    /**
     * 用例执行数
     */
    private Integer execCount;
    /**
     * 用例通过数
     */
    private Integer successCount;

    /**
     * 用例通过数
     */
    private Integer failCount;


    /**
     * 用例忽略数 -- 不需要执行 -- 也不计算在内
     */
    private Integer ignoreCount;

    /**
     * 用例阻塞数
     */
    private Integer blockCount;
}
