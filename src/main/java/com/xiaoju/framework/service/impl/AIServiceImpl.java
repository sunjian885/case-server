package com.xiaoju.framework.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson.JSONObject;
import com.xiaoju.framework.entity.dto.AIResult;
import com.xiaoju.framework.entity.dto.Config;
import com.xiaoju.framework.entity.dto.User;
import com.xiaoju.framework.mapper.AIResultMapper;
import com.xiaoju.framework.mapper.ConfigMapper;
import com.xiaoju.framework.mapper.UserMapper;
import com.xiaoju.framework.service.AIService;
import com.xiaoju.framework.service.DTO.AIResultDTO;
import com.xiaoju.framework.util.HttpUtils;
import com.xiaoju.framework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import javax.annotation.Resource;
import java.util.*;

@Service
public class AIServiceImpl implements AIService {

    private final static Logger LOGGER = LoggerFactory.getLogger(AIServiceImpl.class);

    @Resource
    ConfigMapper configMapper;

    @Resource
    AIResultMapper aiResultMapper;

    @Resource
    UserMapper userMapper;

    @Value("${AI_URL}")
    String AI_URL;


    @Override
    public AIResultDTO getAIResult(String AIType,String AIKey, String content) {
        //LOGGER.info("String AIType={},String AIKey={}, String content={}", AIType, AIKey,  content);
        Long userid = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectByUserid(userid);
        //LOGGER.info("user={}", user);

        //通过AIType获取模版
        Config config = configMapper.getConfig(AIType, AIKey);
        //LOGGER.info("config={}", config);
        String template = config.getValue();
        Map params = new HashMap();
        params.put("content",content);
        String inputText = StringUtils.stringFormat(template,params);
        //根据模版中的变量替换和content中的内容替换成具体的Prompt
        LOGGER.info("inputText={}", inputText);
        LOGGER.info("config={}", config);
        JSONObject request = new JSONObject();
        request.put("input_text",inputText);
        request.put("model_name","openai/gpt-3.5-turbo");
        request.put("prompt",config.getPrompt());
        JSONObject result = HttpUtils.post(AI_URL, request);
        LOGGER.info("result={}", result);
        //TODO: 可以异步执行，不影响主线程
        AIResult aiResult = new AIResult();
        aiResult.setPrompt(inputText);
        aiResult.setUsername(user.getRealName());
        aiResult.setUserid(userid);
        aiResult.setToken(result.getString("msg"));
        aiResult.setType(AIKey);
        aiResult.setGmtUpdated(new Date());
        aiResult.setGmtCreated(new Date());
        aiResult.setStatus(1);
        if (result.getString("msg").equals("success")) {
            aiResult.setResult(result.getJSONObject("data").getString("answer"));
        }else {
            aiResult.setResult("获取gpt结果失败");
        }
        aiResultMapper.insert(aiResult);
        AIResultDTO aiResultDTO = transferAIResultToAiResultDTO(aiResult);
        if (result.getString("msg").equals("success")) {
            aiResultDTO.setSuccess(true);
        }else {
            aiResultDTO.setSuccess(false);
        }
        aiResultDTO.setErrorCode(result.getString("msg"));
        return aiResultDTO;
    }

    @Override
    public List<AIResult> getHistoryResults(String username, Long userid, Long id) {
        List<AIResult> allResult = aiResultMapper.selectAIResults(username, userid, id);
        return allResult;
    }


    public AIResultDTO transferAIResultToAiResultDTO(AIResult aiResult){
        AIResultDTO aiResultDTO = new AIResultDTO();
        aiResultDTO.setResult(aiResult.getResult());
        aiResultDTO.setStatus(String.valueOf(aiResult.getStatus()));
        aiResultDTO.setCallId(String.valueOf(aiResult.getId()));
        aiResultDTO.setToken(aiResult.getToken());
        aiResultDTO.setStartTime(aiResult.getGmtCreated());
        aiResultDTO.setEndTime(aiResult.getGmtUpdated());
        return aiResultDTO;
    }
}
