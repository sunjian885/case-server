package com.xiaoju.framework.service.impl;

import com.xiaoju.framework.entity.dto.User;
import com.xiaoju.framework.entity.persistent.ExecRecordDetail;
import com.xiaoju.framework.entity.persistent.TestCase;
import com.xiaoju.framework.mapper.ExecRecordDetailMapper;
import com.xiaoju.framework.mapper.TestCaseMapper;
import com.xiaoju.framework.mapper.UserMapper;
import com.xiaoju.framework.service.DTO.Statistics;
import com.xiaoju.framework.service.StatisticsService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;



@Service
public class StatisticsServiceImpl implements StatisticsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsServiceImpl.class);

    @Resource
    private TestCaseMapper testCaseMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private ExecRecordDetailMapper execRecordDetailMapper;

    @Override
    public List<Statistics> getExecInfo(List<Long> userids,Date gmtCreatedBegin, Date gmtCreatedEnd) {
        List<Statistics> allStatistics = new ArrayList<Statistics>();
        for (Long userid:userids){
            User user = userMapper.selectByUserid(userid);
            Statistics statistics = new Statistics();
            statistics.setUserid(userid);
            if (!Objects.isNull(user)){
                statistics.setUsername(user.getRealName());
                List<ExecRecordDetail> execRecordDetails = execRecordDetailMapper.searchExecRecordDetailsByUserid(userid, gmtCreatedBegin, gmtCreatedEnd);
                LOGGER.info("execRecordDetails =={}",execRecordDetails);
                if (!CollectionUtils.isEmpty(execRecordDetails)){

                   int execSum = execRecordDetails.stream().mapToInt(ExecRecordDetail::getExecCount).sum();
                   int successSum = execRecordDetails.stream().mapToInt(ExecRecordDetail::getSuccessCount).sum();
                   int failSum = execRecordDetails.stream().mapToInt(ExecRecordDetail::getFailCount).sum();
                   int blockSum = execRecordDetails.stream().mapToInt(ExecRecordDetail::getBlockCount).sum();
                   int ignoreSum = execRecordDetails.stream().mapToInt(ExecRecordDetail::getIgnoreCount).sum();
                   statistics.setBlockCount(blockSum);
                   statistics.setExecCount(execSum);
                   statistics.setIgnoreCount(ignoreSum);
                   statistics.setSuccessCount(successSum);
                   statistics.setFailCount(failSum);
                }
                List<TestCase> testCasesByCreator = testCaseMapper.getTestCasesByCreator(user.getRealName(), gmtCreatedBegin, gmtCreatedEnd);
                if (!CollectionUtils.isEmpty(testCasesByCreator)){

                    int amountSum = testCasesByCreator.stream().mapToInt(TestCase::getAmount).sum();

                    statistics.setAmount(amountSum);
                }

            }
            allStatistics.add(statistics);
        }
        return allStatistics;
    }



}
