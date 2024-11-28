package com.xiaoju.framework.service;

import com.xiaoju.framework.entity.dto.AIResult;
import com.xiaoju.framework.service.DTO.AIResultDTO;

import java.util.List;
import java.util.Map;

//调用chatGPT相关接口服务内容
public interface AIService {

    AIResultDTO getAIResult(String AIType,String AIKey, String content);

    List<AIResult> getHistoryResults(String username, Long userid, Long id);
}
