package com.xiaoju.framework.handler;

import com.alibaba.fastjson.JSONObject;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonDiff;
import com.xiaoju.framework.entity.persistent.CaseBackup;
import com.xiaoju.framework.entity.persistent.TestCase;
import com.xiaoju.framework.mapper.CaseBackupMapper;
import com.xiaoju.framework.mapper.TestCaseMapper;
import com.xiaoju.framework.util.TreeUtil;


import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

public class SaveIngressTask extends IngressTask {
    EditMessage data;
    CaseBackupMapper caseBackupMapper;
    TestCaseMapper testCaseMapper;

    public SaveIngressTask(SocketIOClient client, SocketIOServer socketIOServer, RoomEntity room, ExecutorService executorEgressService,
                           EditMessage data, CaseBackupMapper caseBackupMapper, TestCaseMapper testCaseMapper) {
        super(client, socketIOServer, room, executorEgressService);
        this.data = data;
        this.caseBackupMapper = caseBackupMapper;
        this.testCaseMapper = testCaseMapper;
    }

    @Override
    public void run() {
        // 1.更新内存中的用例
        room.setCaseContent(data.getCaseContent());

        // 2.保存到backup表，手动保存用例
        //LOGGER.info(Thread.currentThread().getName() + ": 手动保存用例。");
        CaseBackup caseBackup = new CaseBackup();
        caseBackup.setCaseId(room.getCaseId());
        caseBackup.setCaseContent(data.getCaseContent());
        caseBackup.setRecordContent("");
        caseBackup.setCreator(client.getHandshakeData().getSingleUrlParam("user"));
        caseBackup.setExtra("");

        // 此处可以与最新的内容比对，如果一致，则不更新backup表，减少版本数量
        List<CaseBackup> caseBackups = caseBackupMapper.selectByCaseId(caseBackup.getCaseId(), null, null);

        // 如果当前已有，则直接返回
        // todo 此处还是用版本信息控制更加合理
        ObjectMapper jsonMapper = new ObjectMapper();
        try {
            if (caseBackups.size() > 0) {
                String caseContent = caseBackups.get(0).getCaseContent();
                if (JsonDiff.asJson(jsonMapper.readTree(caseContent), jsonMapper.readTree(caseBackup.getCaseContent())).size() == 0) { // 表示没有差别
                    LOGGER.info("当前内容已经保存过了，不再重复保存。");
                    return;
                }
            }
        } catch (IOException e) {
            LOGGER.info("json转换异常. 数据继续备份", e);
        }
        int ret = caseBackupMapper.insert(caseBackup);
        if (ret < 1) {
            LOGGER.error("用例备份落库失败. casebackup id: " + caseBackup.getCaseId() + ", case content: " + caseBackup.getCaseContent() );
            return;
        }

        //3.将用户保存的用例在当前用例中
        TestCase testCase = testCaseMapper.selectOne(room.getCaseId());
        if (!testCase.getCaseContent().equals(data.getCaseContent())){
            testCase.setCaseContent(data.getCaseContent());
            //计算用例数量
            Integer leafNodeCount = TreeUtil.getLeafNodeCount(data.getCaseContent());
            testCase.setAmount(leafNodeCount);
            testCase.setGmtModified(new Date());
            String username = client.getHandshakeData().getSingleUrlParam("user");
            testCase.setModifier(username);
            testCase.setModifierId(Long.valueOf(client.getHandshakeData().getSingleUrlParam("userid")));
            testCaseMapper.update(testCase);
        }


        LOGGER.info(Thread.currentThread().getName() + ": 手动保存结束。");
    }
}
