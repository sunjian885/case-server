package com.xiaoju.framework.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.xiaoju.framework.entity.dto.CountsCollect;
import com.xiaoju.framework.entity.dto.User;
import com.xiaoju.framework.entity.persistent.ExecRecord;
import com.xiaoju.framework.entity.persistent.ExecRecordDetail;
import com.xiaoju.framework.entity.xmind.CaseContent;
import com.xiaoju.framework.entity.xmind.RootData;
import com.xiaoju.framework.handler.EditProgress;
import com.xiaoju.framework.mapper.ExecRecordDetailMapper;
import com.xiaoju.framework.mapper.ExecRecordMapper;
import com.xiaoju.framework.mapper.UserMapper;
import com.xiaoju.framework.service.ExecRecordDetailService;
import com.xiaoju.framework.util.TreeUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ExecRecordDetailServiceImpl implements ExecRecordDetailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecRecordDetailServiceImpl.class);

    @Resource
    private ExecRecordDetailMapper execRecordDetailMapper;

    @Resource
    private ExecRecordMapper execRecordMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    public List<ExecRecordDetail> queryExecRecordDetailByUseridAndRecordId(Long userid, Long recordId) {

        List<ExecRecordDetail> result = null;
        List<ExecRecordDetail> execRecordDetails = execRecordDetailMapper.searchDetailsByRecordIdAndUserid(recordId, userid);

        if (execRecordDetails.size() > 0){
            result = execRecordDetails;
        }
        return result;
    }

    @Override
    public boolean saveExecRecordDetail(ExecRecordDetail execRecordDetail) {
        //LOGGER.info(" execRecordDetail = {}",execRecordDetail);
        int insert = execRecordDetailMapper.insert(execRecordDetail);
        //LOGGER.info(" insert = {}",insert);
        return insert >0;
    }

    @Override
    public boolean updateExecRecordDetail(ExecRecordDetail execRecordDetail, Integer oldVersion) {
        int update = execRecordDetailMapper.update(execRecordDetail,oldVersion);
        return update>0;
    }

    /**
     * 判断所有操作的节点必须是叶子节点，如果不是叶子节点就抛弃
     * @param data
     * @param caseContent
     * @return
     */
    @Override
    public String convertToJSONString(EditProgress data, String caseContent) {

        CaseContent content = JSONObject.parseObject(caseContent, CaseContent.class);
        RootData root = content.getRoot();
        List<String> allLeafNode = TreeUtil.getAllLeafNode(root);
        //找到所有节点是叶子节点的node
        List<String> collect = data.getProgressIds().stream().filter(allLeafNode::contains).collect(Collectors.toList());

        JSONObject jsonObject = new JSONObject();
        collect.parallelStream().forEach(t ->{
             jsonObject.put(t,data.getProgress());
         });
        return jsonObject.toJSONString();
    }

    @Override
    public Boolean clearRecord(Long recordId) {
        LOGGER.info("清理record id是{}",recordId);
        List<ExecRecordDetail> execRecordDetails = execRecordDetailMapper.searchDetailsByRecordIdAndUserid(recordId, null);

        LOGGER.info("清理内容{}",execRecordDetails);
        for(ExecRecordDetail execRecordDetail: execRecordDetails){
            Integer oldVersion = execRecordDetail.getVersion();
            execRecordDetail.setIsDelete(1);
            execRecordDetail.setGmtModified(new Date());
            execRecordDetail.setVersion(oldVersion+1);
            execRecordDetailMapper.update(execRecordDetail,oldVersion);
        }
        return true;
    }

    @Override
    public void fixExecRecordDetail() {
        List<ExecRecord> allRecordList = execRecordMapper.getAllRecordList();
        try{
            for (ExecRecord execRecord:allRecordList){
                ExecRecordDetail execRecordDetail = convertToDetail(execRecord);
                execRecordDetailMapper.insert(execRecordDetail);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }




    public ExecRecordDetail convertToDetail(ExecRecord execRecord){
        String caseContent = execRecord.getCaseContent();
        CountsCollect countsCollect = TreeUtil.getProgressCountsCollect(caseContent);
        User user = userMapper.selectByRealName(execRecord.getCreator());

        ExecRecordDetail execRecordDetail = new ExecRecordDetail();
        execRecordDetail.setRecordId(execRecord.getId());
        if (Objects.isNull(caseContent) || StringUtils.isEmpty(caseContent)){
            caseContent = "{}";
        }
        if (!Objects.isNull(user)){
            execRecordDetail.setUserid(user.getUserid());
        }
        execRecordDetail.setCaseContent(caseContent);
        execRecordDetail.setCaseId(execRecord.getCaseId());
        execRecordDetail.setUsername(execRecord.getCreator());

        execRecordDetail.setExecCount(countsCollect.getExecCount());
        execRecordDetail.setFailCount(countsCollect.getFailCount());
        execRecordDetail.setBlockCount(countsCollect.getBlockCount());
        execRecordDetail.setIgnoreCount(countsCollect.getIgnoreCount());
        execRecordDetail.setSuccessCount(countsCollect.getSuccessCount());

        execRecordDetail.setGmtCreated(new Date());
        execRecordDetail.setGmtModified(new Date());
        execRecordDetail.setVersion(0);
        execRecordDetail.setEnv(0);
        execRecordDetail.setIsDelete(execRecord.getIsDelete());

        return execRecordDetail;
    }
}
