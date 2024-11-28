package com.xiaoju.framework.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.alibaba.fastjson.JSONObject;
import com.xiaoju.framework.entity.persistent.TestCase;
import com.xiaoju.framework.entity.xmind.CaseContent;
import com.xiaoju.framework.entity.xmind.CaseCount;
import com.xiaoju.framework.mapper.TestCaseMapper;
import com.xiaoju.framework.service.DTO.Statistics;
import com.xiaoju.framework.service.StatisticsService;
import com.xiaoju.framework.util.TreeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/api/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/getCaseInfos")
    @SaIgnore
    public List<Statistics> getAllCaseInfo(@RequestParam("userids") List<Long> userids,
                                  @RequestParam("gmtCreateBegin") String gmtCreateBegin,
                                  @RequestParam("gmtCreatedEnd") String gmtCreatedEnd){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date begin = sdf.parse(gmtCreateBegin, new ParsePosition(0));
        Date end = sdf.parse(gmtCreatedEnd, new ParsePosition(0));
        List<Statistics> execInfo = statisticsService.getExecInfo(userids, begin, end);
        return execInfo;
    }


    @GetMapping("/getLeafs")
    public List<String> getAllLeafs(){
        //String creator = "任江涛";
        //List<TestCase> allCase = caseMapper.getTestCasesByCreator(creator, null, null);
        //for (TestCase testCase : allCase) {
        //    CaseContent content = JSONObject.parseObject(testCase.getCaseContent(), CaseContent.class);
        //    List<String> allLeafNode = TreeUtil.getAllLeafNode(content.getRoot());
        //    log.info("leaf nodes is =={}，size is =={}",allLeafNode,allLeafNode.size());
        //}
        return null;
    }





}
