package com.xiaoju.framework.entity.dto;

import lombok.Data;

@Data
public class CountsCollect {

    /**
     * 用例执行数
     */
    private Integer execCount = 0;
    /**
     * 用例通过数
     */
    private Integer successCount = 0;

    /**
     * 用例忽略数 -- 不需要执行 -- 也不计算在内
     */
    private Integer ignoreCount = 0;

    /**
     * 用例阻塞数
     */
    private Integer blockCount = 0;

    /**
     * 用例失败数
     */
    private Integer failCount = 0;

    public void addIgnoreCount(){
        this.ignoreCount++;
    }

    public void addSuccessCount(){
        this.successCount++;
        this.execCount++;
    }

    public void addFailCount(){
        this.failCount++;
        this.execCount++;
    }

    public void addBlockCount(){
        this.blockCount++;
        this.execCount++;
    }


}
