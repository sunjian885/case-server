package com.xiaoju.framework.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.xiaoju.framework.entity.dto.CountsCollect;
import com.xiaoju.framework.entity.persistent.ExecRecordDetail;
import com.xiaoju.framework.service.ExecRecordDetailService;
import com.xiaoju.framework.util.TreeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class EditProgressTask extends IngressTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(EditProgressTask.class);


    ExecRecordDetailService execRecordDetailService;
    EditProgress data;

    public EditProgressTask(SocketIOClient client, SocketIOServer socketIOServer, RoomEntity room, ExecutorService executorEgressService, EditProgress data, ExecRecordDetailService execRecordDetailService) {
        super(client, socketIOServer, room, executorEgressService);
        this.data = data;
        this.execRecordDetailService = execRecordDetailService;
    }

    @Override
    public void run() {

        ClientEntity clientEntity = getRoomFromClient(client);
        String roomId = clientEntity.getRoomId();
        //LOGGER.info("EditProgressTask start");
        //LOGGER.info("EditProgressTask data  = {}",data);
        //LOGGER.info("EditProgressTask clientEntity  = {}",clientEntity);
        //LOGGER.info("EditProgressTask roomId  = {}",roomId);
        //LOGGER.info("EditProgressTask room  = {}",room.getCaseContent());
        //LOGGER.info("clientEntity.recordId > 0  = {}",clientEntity.recordId > 0);
        //LOGGER.info("clientEntity.userid > 0  = {}",clientEntity.userid > 0);
        /**
         *  保存执行记录步骤
         *      1. 先查询数据库中是否有记录
         *          i. 无记录就插入记录
         *          ii. 有记录，就更新case_Content 和 gmt_modify
         */
        if (clientEntity.userid > 0 && clientEntity.recordId > 0) {
            LOGGER.info("EditProgressTask 开始执行");
            List<ExecRecordDetail> dbExecRecordDetails = execRecordDetailService.queryExecRecordDetailByUseridAndRecordId(null, clientEntity.recordId);
            String caseContent = execRecordDetailService.convertToJSONString(data, room.getCaseContent());
            //没有记录，插入数据
            if (CollectionUtils.isEmpty(dbExecRecordDetails)) {
                this.insertExecRecordDetail(caseContent, clientEntity);
            } else {
                //更新同一个record id 相关问诊记录
                List<Long> userids = dbExecRecordDetails.stream().map(ExecRecordDetail::getUserid).collect(Collectors.toList());
                //如果还没有记录
                if (!userids.contains(clientEntity.getUserid())) {
                    //保存进入数据库,当前用户数据保存进数据库
                    this.insertExecRecordDetail(caseContent, clientEntity);
                    //清除其他人数据表中的这些节点，操作人已经换了
                    for (ExecRecordDetail execRecordDetail : dbExecRecordDetails) {
                        JSONObject jsonObject = JSON.parseObject(execRecordDetail.getCaseContent());
                        for (String key : jsonObject.keySet()) {
                            if (data.getProgressIds().contains(key)) {
                                jsonObject.remove(key);
                            }
                        }
                        // 处理完之后对比前后内容，内容一致不操作，内容不一致的话，version升1，然后更新内容和时间
                        if (!jsonObject.toJSONString().equals(execRecordDetail.getCaseContent())) {
                            this.updateExecRecordDetail(jsonObject,execRecordDetail);
                        }
                    }
                } else{
                    for (ExecRecordDetail execRecordDetail : dbExecRecordDetails) {
                        //LOGGER.info("遍历的内容为：{}", execRecordDetail);
                        JSONObject jsonObject = JSON.parseObject(execRecordDetail.getCaseContent());

                        if (Objects.equals(execRecordDetail.getUserid(), clientEntity.getUserid())) {

                            if(Objects.isNull(data.getProgress())){
                                //操作的progress为空的时候，就是删除当前节点
                                /**
                                 * 使用for循环删除的时候，会抛出java.util.ConcurrentModificationException异常，多线程下使用list和map的时候会出现这样的问题
                                 * 参考：https://www.cnblogs.com/owenma/p/13453840.html
                                 */
                                Iterator<String> iterator = jsonObject.keySet().iterator();
                                while(iterator.hasNext()){
                                    String key = iterator.next();
                                    if (data.getProgressIds().contains(key)) {
                                        iterator.remove();
                                    }
                                }
                            }else {
                                JSONObject jsonContent = JSONObject.parseObject(caseContent);
                                Set<String> clientProgress = jsonContent.keySet();
                                //将操作内容直接赋值到json中
                                for (String key : clientProgress) {
                                    jsonObject.put(key, data.getProgress());
                                }
                            }
                            //找到满足条件的节点然后赋值给caseContent
                            LOGGER.info("userid一致的时候，jsonObject要更新的内容：{}", jsonObject.toJSONString());
                            //对比操作前后的数据，如果一致，就不修改，如果不一致，就更新数据，并且version+1
                            if (!jsonObject.toJSONString().equals(execRecordDetail.getCaseContent())) {
                                this.updateExecRecordDetail(jsonObject,execRecordDetail);
                            }

                        } else {
                            /**
                             * 使用for循环删除的时候，会抛出java.util.ConcurrentModificationException异常，多线程下使用list和map的时候会出现这样的问题
                             * 参考：https://www.cnblogs.com/owenma/p/13453840.html
                             */
                            Iterator<String> iterator = jsonObject.keySet().iterator();
                            while(iterator.hasNext()){
                                String key = iterator.next();
                                if (data.getProgressIds().contains(key)) {
                                    iterator.remove();
                                }
                            }
                            //LOGGER.info("userid不一致的时候，jsonObject要更新的内容：{}", jsonObject.toJSONString());
                            // 处理完之后对比前后内容，内容一致不操作，内容不一致的话，version升1，然后更新内容和时间
                            if (!jsonObject.toJSONString().equals(execRecordDetail.getCaseContent())) {
                                this.updateExecRecordDetail(jsonObject,execRecordDetail);
                            }
                        }
                    }
                }
            }
        }


    }

    public void insertExecRecordDetail(String caseContent, ClientEntity clientEntity) {
        CountsCollect countsCollect = TreeUtil.getProgressCountsCollect(caseContent);
        ExecRecordDetail insert = new ExecRecordDetail();
        insert.setCaseContent(caseContent);
        insert.setEnv(0);
        insert.setUsername(clientEntity.getUsername());
        insert.setUserid(clientEntity.getUserid());
        insert.setCaseId(clientEntity.getCaseId());
        insert.setRecordId(clientEntity.getRecordId());
        insert.setExecCount(countsCollect.getExecCount());
        insert.setVersion(0);
        insert.setIgnoreCount(countsCollect.getIgnoreCount());
        insert.setSuccessCount(countsCollect.getSuccessCount());
        insert.setIsDelete(0);
        insert.setBlockCount(countsCollect.getBlockCount());
        insert.setFailCount(countsCollect.getFailCount());
        execRecordDetailService.saveExecRecordDetail(insert);
    }


    public void updateExecRecordDetail(JSONObject jsonObject, ExecRecordDetail execRecordDetail){
        String updateContent = jsonObject.toJSONString();
        CountsCollect updateCount = TreeUtil.getProgressCountsCollect(updateContent);
        ExecRecordDetail update = new ExecRecordDetail();
        update.setId(execRecordDetail.getId());
        update.setCaseContent(updateContent);
        update.setUsername(execRecordDetail.getUsername());
        update.setUserid(execRecordDetail.getUserid());
        update.setCaseId(execRecordDetail.getCaseId());
        update.setRecordId(execRecordDetail.getRecordId());
        update.setExecCount(updateCount.getExecCount());
        update.setVersion(execRecordDetail.getVersion() + 1);
        update.setIgnoreCount(updateCount.getIgnoreCount());
        update.setSuccessCount(updateCount.getSuccessCount());
        update.setBlockCount(updateCount.getBlockCount());
        update.setFailCount(updateCount.getFailCount());
        execRecordDetailService.updateExecRecordDetail(update, execRecordDetail.getVersion());
    }

    public static void main(String[] args) {
        JSONObject jsonObject = JSON.parseObject("{\"coui5ht11nk0\":1,\"copi7dpmiz40\":1,\"copi7apa0p40\":1,\"copieooqicw0\":1,\"copieu5tls00\":1}");
        List progressList = new ArrayList();
        progressList.add("coui5ht11nk0");
        progressList.add("copi7dpmiz40");
        System.out.println("jsonObject = " + jsonObject.toJSONString());
        System.out.println("jsonObject.keySet() = " + jsonObject.keySet());
        try{
            Iterator<String> iterator = jsonObject.keySet().iterator();
            while(iterator.hasNext()){
                String key = iterator.next();
                if (progressList.contains(key)) {
                    iterator.remove();
                }
            }
            System.out.println("处理后的jsonObject = " + jsonObject.toJSONString());
        }catch (Exception e){
            e.printStackTrace();
        }

    }

}
