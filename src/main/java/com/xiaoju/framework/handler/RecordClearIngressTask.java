package com.xiaoju.framework.handler;

import com.corundumstudio.socketio.BroadcastOperations;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flipkart.zjsonpatch.JsonDiff;
import com.xiaoju.framework.service.ExecRecordDetailService;

import java.util.Iterator;
import java.util.concurrent.ExecutorService;

public class RecordClearIngressTask extends IngressTask {

    ExecRecordDetailService execRecordDetailService;

    public RecordClearIngressTask(SocketIOClient client, SocketIOServer socketIOServer, RoomEntity room, ExecutorService executorEgressService, ExecRecordDetailService execRecordDetailService) {
        super(client, socketIOServer, room, executorEgressService);
        this.execRecordDetailService = execRecordDetailService;
    }

    @Override
    public void run() {
        BroadcastOperations broadcastOperations = socketIOServer.getRoomOperations(room.getRoomId());
        String recordId = client.getHandshakeData().getSingleUrlParam("recordId");
        String caseCurrent = room.getCaseContent();

        try {
            JsonNode caseObj = jsonMapper.readTree(caseCurrent);
            JsonNode caseTarget = caseObj.deepCopy();
            traverse(caseTarget);
            ArrayNode patchNotify = (ArrayNode) JsonDiff.asJson(caseObj, caseTarget);
            executorEgressService.submit(new NotifyEgressTask("edit_notify_event", PushMessage.builder().message(patchNotify.toString()).build(), broadcastOperations));

            room.setCaseContent(caseTarget.toString());
            LOGGER.info("执行清理操作");
            execRecordDetailService.clearRecord(Long.valueOf(recordId));

        } catch (Exception e) {

        }
    }

    private void traverse(JsonNode caseObj) {
        Iterator<JsonNode> iterator = caseObj.iterator();

        while (iterator.hasNext()) {
            JsonNode n = iterator.next();
            if (n.size() > 0) {
                if (n.has("progress")) {
                    ((ObjectNode) n).remove("progress");
                }
                traverse(n);
            } else {
//                 System.out.println(n.toString());
            }
        }
    }
}
