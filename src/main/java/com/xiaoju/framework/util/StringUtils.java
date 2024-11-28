package com.xiaoju.framework.util;

import com.alibaba.fastjson.JSONObject;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {


    public static String stringFormat(String template, Map params){
        Matcher matcher = Pattern.compile("\\$\\{\\w+\\}").matcher(template);
        StringBuffer stringBuffer = new StringBuffer();
        while (matcher.find()){
            String param = matcher.group();
            Object value = params.get(param.substring(2,param.length()-1));
            matcher.appendReplacement(stringBuffer,value==null?"":value.toString());
        }
        matcher.appendTail(stringBuffer);
        return stringBuffer.toString();
    }


    public static void main(String[] args) {
        //String template = "验证码:${code},您正在登录管理后台${ttt}，5分钟内输入${yyy}有效。";
        //Map params = new HashMap<>();
        //params.put("code",1234);
        //params.put("ttt","天王盖地虎");
        //params.put("yyy","天天向上");
        //String s = stringFormat(template, params);
        //System.out.println("s = " + s);


        //发送http请求
        String url = "https://algo-platform.test.shantaijk.cn/gpt/single-turn";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        JSONObject params = new JSONObject();
        params.put("content","怎么才能保证在A股中获利");
        params.put("accessKey","dc3e5d7aa8b1d8eaa9445c4f8ed04854");
        HttpEntity<String> request = new HttpEntity<>(params.toString(), httpHeaders);
        ResponseEntity<JSONObject> exchange = restTemplate.exchange(url, HttpMethod.POST, request, JSONObject.class);
        System.out.println("exchange.getBody() = " + exchange.getBody());

        params.put("token",exchange.getBody().getString("token"));
        HttpEntity<String> request2 = new HttpEntity<>(params.toString(), httpHeaders);
        for (int i=0;i<100;i++){
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("request2 = " + request2.getBody());
            ResponseEntity<JSONObject> exchange2 = restTemplate.exchange(url, HttpMethod.POST, request2, JSONObject.class);
            System.out.println("exchange2.getBody() = " + exchange2.getBody());
            if (exchange2.getBody().getString("status").equals("COMPLETED")){
                //System.out.println("exchange2.getBody() = " + exchange2.getBody());
                break;
            }
        }

    }
}
