package com.shantai.tool;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;


@SpringBootTest
public class DingdingMsgTool {

    public static final String SECRET = "SEC95cb81a976c74cee2d75b40e688bfe039c38d6eab267ad70e0224ed4e1e6fcdb";

    public static final String WEBHOOK = "https://oapi.dingtalk.com/robot/send?access_token=7c1e74664f69222a969b4dbdb21620aa1db5e89fb525119ce95610da05a35c9f";



    public static String getSign(Long timestamp) {
        // 获取系统时间戳
        //Long timestamp = System.currentTimeMillis();
        // 拼接
        String stringToSign = timestamp + "\n" + SECRET;
        // 使用HmacSHA256算法计算签名
        Mac mac = null;
        String sign = null;
        try {
            mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            // 进行Base64 encode 得到最后的sign，可以拼接进url里
            sign = URLEncoder.encode(new String(Base64.encodeBase64(signData)), "UTF-8");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return sign;
    }


    public static String getDingUrl(String sign,Long timestamp){
        return WEBHOOK + "&timestamp=" + timestamp + "&sign=" + sign;
    }

    public void sendMsg(String content) throws IOException {
        Long timestamp = System.currentTimeMillis();
        JSONObject textContent = new JSONObject();
        textContent.put("content",content);
        JSONObject map = new JSONObject();
        map.put("msgtype","text");
        map.put("text",textContent.toJSONString());
        System.out.println(map.toJSONString());
        StringEntity stringEntity = new StringEntity(map.toJSONString(),StandardCharsets.UTF_8);
        stringEntity.setContentType("application/json");
        HttpPost httpPost = new HttpPost(getDingUrl(getSign(timestamp),timestamp));
        httpPost.setEntity(stringEntity);
        CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
        CloseableHttpResponse response = closeableHttpClient.execute(httpPost);
        HttpEntity entity = response.getEntity();
        String responseContent = EntityUtils.toString(entity);
        System.out.println("responseContent = " + responseContent);
    }


    @Test()
    public void test() throws IOException {
        DingdingMsgTool dingdingMsgTool = new DingdingMsgTool();
        dingdingMsgTool.sendMsg("好好写作业");
    }
}
