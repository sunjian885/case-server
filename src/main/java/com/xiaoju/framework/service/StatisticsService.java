package com.xiaoju.framework.service;

import com.xiaoju.framework.service.DTO.Statistics;

import java.util.Date;
import java.util.List;

public interface StatisticsService {

    List<Statistics> getExecInfo(List<Long> userids, Date gmtCreatedBegin, Date gmtCreatedEnd);


}
