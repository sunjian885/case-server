package com.xiaoju.framework.util;

import com.alibaba.fastjson.JSONObject;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

public class HttpUtils {

    public static RestTemplate restTemplate = new RestTemplate();

    public static JSONObject post(String url, JSONObject params){

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(params.toString(), httpHeaders);
        ResponseEntity<JSONObject> exchange = restTemplate.exchange(url, HttpMethod.POST, request, JSONObject.class);
        return exchange.getBody();
    }
}
