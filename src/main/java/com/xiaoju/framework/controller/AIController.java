package com.xiaoju.framework.controller;


import cn.dev33.satoken.annotation.SaIgnore;
import com.xiaoju.framework.controller.request.AIRequest;
import com.xiaoju.framework.controller.request.HistoryRequest;
import com.xiaoju.framework.entity.dto.AIResult;
import com.xiaoju.framework.entity.response.controller.Response;
import com.xiaoju.framework.service.AIService;
import com.xiaoju.framework.service.DTO.AIResultDTO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/api/ai")
public class AIController {


    @Resource
    AIService aiService;

    /**
     * 获取AI结果
     * @return 响应体
     */
    @PostMapping(value = "/getAIResult")
    @SaIgnore
    public Response<AIResultDTO> getAIResult(@RequestBody AIRequest aiRequest) {
        return Response.success(aiService.getAIResult(aiRequest.getAitype(),aiRequest.getAikey(),aiRequest.getContent()));
    }


    /**
     * 查询历史gpt结果
     * @return 响应体
     */
    @PostMapping(value = "/getHistoryResults")
    @SaIgnore
    public Response<List<AIResult>> getHistoryResults(@RequestBody HistoryRequest historyRequest) {
        return Response.success(aiService.getHistoryResults(historyRequest.getUsername(),historyRequest.getUserid(),historyRequest.getId()));
    }

}
