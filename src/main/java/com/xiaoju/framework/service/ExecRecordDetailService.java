package com.xiaoju.framework.service;

import com.xiaoju.framework.entity.persistent.ExecRecordDetail;
import com.xiaoju.framework.handler.EditProgress;

import java.util.List;

public interface ExecRecordDetailService {


    List<ExecRecordDetail> queryExecRecordDetailByUseridAndRecordId(Long userid, Long recordId);

    boolean saveExecRecordDetail(ExecRecordDetail execRecordDetail);

    boolean updateExecRecordDetail(ExecRecordDetail execRecordDetail , Integer oldVersion);

    /**
     * 将操作数据转成JSON格式的字符串
     * @param data
     * @return
     */
    String convertToJSONString(EditProgress data, String caseContent);

    Boolean clearRecord(Long recordId);

    void fixExecRecordDetail();
}
