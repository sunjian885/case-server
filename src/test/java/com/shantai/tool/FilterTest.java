package com.shantai.tool;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xiaoju.framework.entity.xmind.IntCount;
import com.xiaoju.framework.util.TreeUtil;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Stack;


public class FilterTest {




    /**
     * 回溯json链路上的节点，没有其他节点的就删除
     * @param stackCheck
     */
    public static void rollbackDel(Stack<JSONObject> stackCheck,Stack<IntCount> intCounts) {
        int size = stackCheck.size();
        for (int i = 0; i < size-1; i++) {
            JSONObject top = stackCheck.peek();
            if (top.getJSONArray("children").size() == 0) {
                stackCheck.pop();
                stackCheck.peek().getJSONArray("children").remove(top);
                intCounts.pop().del();
            } else {
                break;
            }
        }
    }



    static boolean isPriorityIn(Integer data, List<String> priorities) {
        for (String priority : priorities) {
            if (data != null && data.equals(Integer.parseInt(priority))) {
                return true;
            }
        }
        return false;
    }

    //筛选用例
    public static void filterProgress(JSONObject caseContent, List<Integer> progresss, Stack<JSONObject> caseStack, Stack<IntCount> intCounts){
        JSONArray children = caseContent.getJSONArray("children");
        IntCount count = new IntCount(0);
        for ( ;count.get() < children.size(); ){
            JSONObject obj = (JSONObject) children.get(count.get());
            count.add();
            //有progress的用例，这些用例需要保留，不需要处理
            if (obj.getJSONObject("data").containsKey("progress") &&
                    obj.getJSONObject("data").getInteger("progress") != null &&
                    progresss.contains(obj.getJSONObject("data").getInteger("progress")) ){
                continue;
            }else{
                if (Objects.isNull(obj.getJSONArray("children")) || obj.getJSONArray("children").size() == 0) { // 当前是叶子结点
                    //System.out.println("叶子节点obj = " + obj); //不满足条件的节点
                    children.remove(obj);
                    count.del();

                    rollbackDel(caseStack,intCounts);
                } else {
                    caseStack.push(obj);
                    intCounts.push(count);
                    filterProgress(obj, progresss, caseStack,intCounts);
                }
            }
        }
    }

    //将所有的叶子节点中的progress赋值,将没有执行的叶子节点的progress赋值成100
    public void convertNotoProgress(JSONObject root){
        if(Objects.isNull(root.getJSONArray("children")) || root.getJSONArray("children").size()==0){
            if (!root.getJSONObject("data").containsKey("progress") ||
                    root.getJSONObject("data").getInteger("progress") == null ){
                root.getJSONObject("data").put("progress",100);
            }
        }else {
            root.getJSONArray("children").stream().forEach(item ->{
                convertNotoProgress((JSONObject)item);
            });
        }
    }

    //将所有的叶子节点中的progress值等于100的还原成没有progress
    public void convertProgressToNot(JSONObject root){
        if(Objects.isNull(root.getJSONArray("children")) || root.getJSONArray("children").size()==0){
            if (root.getJSONObject("data").containsKey("progress") &&
                    root.getJSONObject("data").getInteger("progress") != null &&
                    root.getJSONObject("data").getInteger("progress") == 100){
                root.getJSONObject("data").remove("progress");
            }
        }else {
            root.getJSONArray("children").stream().forEach(item ->{
                convertProgressToNot((JSONObject)item);
            });
        }
    }

    @Test
    public void test(){
        String caseContent = "{\"template\":\"right\",\"root\":{\"data\":{\"image\":\"\",\"created\":\"1634006790476\",\"id\":\"0ggnje1shnb52bmu39q8raucic\",\"imageSize\":{},\"text\":\"20201014发布工作台相关\",\"priority\":0},\"children\":[{\"data\":{\"image\":\"\",\"created\":\"1634006790474\",\"id\":\"1fhbasksbo0222f6tfbp6tg7nt\",\"imageSize\":{},\"text\":\"视频问诊异常处理\",\"priority\":0},\"children\":[{\"data\":{\"image\":\"\",\"created\":\"1634006790472\",\"id\":\"737ljc5esldcka4bde6gg1hasi\",\"imageSize\":{},\"text\":\"问诊完成后\",\"priority\":0},\"children\":[{\"data\":{\"image\":\"\",\"created\":\"1634006790472\",\"progress\":1,\"id\":\"3q7so0rtlp9dafbu9l5hnr8e1s\",\"imageSize\":{},\"text\":\"工作台关闭视频问诊对话框\",\"priority\":0},\"children\":[]}]},{\"data\":{\"image\":\"\",\"created\":\"1634006790472\",\"id\":\"72joluq93tdml2ssg4qj8poium\",\"imageSize\":{},\"text\":\"视频问诊信息素\",\"priority\":0},\"children\":[{\"data\":{\"image\":\"\",\"created\":\"1634006790472\",\"progress\":1,\"id\":\"6876m1ra3upu0dj9fvfna8ijh2\",\"imageSize\":{},\"text\":\"子主题 1\",\"priority\":0},\"children\":[]}]},{\"data\":{\"image\":\"\",\"created\":\"1634006790474\",\"progress\":1,\"id\":\"71ak6r62kjp9j6rlgvk1k6klqo\",\"imageSize\":{},\"text\":\"工作台新增异常结束场景\",\"priority\":0},\"children\":[{\"data\":{\"image\":\"\",\"created\":\"1634006790473\",\"progress\":9,\"id\":\"461j63btk9d3o0invpc5uc7ug2\",\"imageSize\":{},\"text\":\"点击异常挂断\",\"priority\":0},\"children\":[{\"data\":{\"image\":\"\",\"created\":\"1634006790473\",\"progress\":5,\"id\":\"46bei8d2mga97lvouglrkua0ll\",\"imageSize\":{},\"text\":\"返还权益\",\"priority\":0},\"children\":[]}]}]}]},{\"data\":{\"image\":\"\",\"created\":\"1634006790474\",\"id\":\"1ntsmjtkbjia4f1uaeci7htiim\",\"imageSize\":{},\"text\":\"权益作废\",\"priority\":0},\"children\":[{\"data\":{\"image\":\"\",\"created\":\"1634006790474\",\"progress\":9,\"id\":\"6ev6p3p2a7ts8njhc7gmuk8he3\",\"imageSize\":{},\"text\":\"用户拉人入群不会收到话术和邀请家人卡片\",\"priority\":0},\"children\":[]}]},{\"data\":{\"image\":\"\",\"created\":\"1634006790475\",\"id\":\"7glf6fub1lahdaqcaunjb4lfgl\",\"imageSize\":{},\"text\":\"删除副权益人\",\"priority\":0},\"children\":[{\"data\":{\"image\":\"\",\"created\":\"1634006790475\",\"id\":\"484breebnnfa127hshrsoba517\",\"imageSize\":{},\"text\":\"副权益人被删除后\",\"priority\":0},\"children\":[{\"data\":{\"image\":\"\",\"created\":\"1634006790475\",\"progress\":4,\"id\":\"6qrnvsj0m4g4j92ii1qkvhml1m\",\"imageSize\":{},\"text\":\"工作台不能查看到\",\"priority\":0},\"children\":[]}]}]},{\"data\":{\"created\":1663671892545,\"progress\":9,\"id\":\"cn16gsfaf340\",\"text\":\"范德萨范德萨\"},\"children\":[{\"data\":{\"created\":1663724574915,\"id\":\"cn1p52ddblk0\",\"text\":\"知道了\"},\"children\":[]}]},{\"data\":{\"created\":1663672785550,\"progress\":5,\"id\":\"cn16s6ny7lk0\",\"text\":\"呢能\"},\"children\":[]},{\"data\":{\"created\":1663672788421,\"id\":\"cn16s7zft9c0\",\"text\":\"2\"},\"children\":[{\"data\":{\"created\":1663724637530,\"progress\":9,\"id\":\"cn1p5v4wic00\",\"text\":\"哈哈哈哈\"},\"children\":[]}]},{\"data\":{\"created\":1663725392976,\"id\":\"cn1pfi6lqy80\",\"text\":\"nihao \"},\"children\":[{\"data\":{\"created\":1663725543157,\"id\":\"cn1phf6bll40\",\"text\":\"我就是想试试\"},\"children\":[]}]}]},\"theme\":\"fresh-blue\",\"version\":\"1.4.43\",\"base\":34}";
        JSONObject caseObj = JSONObject.parseObject(caseContent);
        JSONObject root = caseObj.getJSONObject("root");
        Stack<JSONObject> stack = new Stack<>();
        stack.push(root);
        Stack<IntCount> intCounts = new Stack<>();
        List<Integer> progresss = Arrays.asList(9);
        filterProgress(root,progresss,stack,intCounts);
        System.out.println(" = = = = = = = = = = = = = = == = = = = = == = = = = = = = = = = = ==");
        System.out.println("root = " + root);
        System.out.println(" = = = = = = = = = = = = = = == = = = = = == = = = = = = = = = = = ==");


    }

    @Test
    public void test3(){
        String caseContent = "{\"template\":\"right\",\"root\":{\"data\":{\"image\":\"\",\"created\":\"1634006790476\",\"id\":\"0ggnje1shnb52bmu39q8raucic\",\"imageSize\":{},\"text\":\"20201014发布工作台相关\",\"priority\":0},\"children\":[{\"data\":{\"image\":\"\",\"created\":\"1634006790474\",\"id\":\"1fhbasksbo0222f6tfbp6tg7nt\",\"imageSize\":{},\"text\":\"视频问诊异常处理\",\"priority\":0},\"children\":[{\"data\":{\"image\":\"\",\"created\":\"1634006790472\",\"id\":\"737ljc5esldcka4bde6gg1hasi\",\"imageSize\":{},\"text\":\"问诊完成后\",\"priority\":0},\"children\":[{\"data\":{\"image\":\"\",\"created\":\"1634006790472\",\"progress\":1,\"id\":\"3q7so0rtlp9dafbu9l5hnr8e1s\",\"imageSize\":{},\"text\":\"工作台关闭视频问诊对话框\",\"priority\":0},\"children\":[]}]},{\"data\":{\"image\":\"\",\"created\":\"1634006790472\",\"id\":\"72joluq93tdml2ssg4qj8poium\",\"imageSize\":{},\"text\":\"视频问诊信息素\",\"priority\":0},\"children\":[{\"data\":{\"image\":\"\",\"created\":\"1634006790472\",\"progress\":1,\"id\":\"6876m1ra3upu0dj9fvfna8ijh2\",\"imageSize\":{},\"text\":\"子主题 1\",\"priority\":0},\"children\":[]}]},{\"data\":{\"image\":\"\",\"created\":\"1634006790474\",\"progress\":1,\"id\":\"71ak6r62kjp9j6rlgvk1k6klqo\",\"imageSize\":{},\"text\":\"工作台新增异常结束场景\",\"priority\":0},\"children\":[{\"data\":{\"image\":\"\",\"created\":\"1634006790473\",\"progress\":9,\"id\":\"461j63btk9d3o0invpc5uc7ug2\",\"imageSize\":{},\"text\":\"点击异常挂断\",\"priority\":0},\"children\":[{\"data\":{\"image\":\"\",\"created\":\"1634006790473\",\"progress\":5,\"id\":\"46bei8d2mga97lvouglrkua0ll\",\"imageSize\":{},\"text\":\"返还权益\",\"priority\":0},\"children\":[]}]}]}]},{\"data\":{\"image\":\"\",\"created\":\"1634006790474\",\"id\":\"1ntsmjtkbjia4f1uaeci7htiim\",\"imageSize\":{},\"text\":\"权益作废\",\"priority\":0},\"children\":[{\"data\":{\"image\":\"\",\"created\":\"1634006790474\",\"progress\":9,\"id\":\"6ev6p3p2a7ts8njhc7gmuk8he3\",\"imageSize\":{},\"text\":\"用户拉人入群不会收到话术和邀请家人卡片\",\"priority\":0},\"children\":[]}]},{\"data\":{\"image\":\"\",\"created\":\"1634006790475\",\"id\":\"7glf6fub1lahdaqcaunjb4lfgl\",\"imageSize\":{},\"text\":\"删除副权益人\",\"priority\":0},\"children\":[{\"data\":{\"image\":\"\",\"created\":\"1634006790475\",\"id\":\"484breebnnfa127hshrsoba517\",\"imageSize\":{},\"text\":\"副权益人被删除后\",\"priority\":0},\"children\":[{\"data\":{\"image\":\"\",\"created\":\"1634006790475\",\"progress\":4,\"id\":\"6qrnvsj0m4g4j92ii1qkvhml1m\",\"imageSize\":{},\"text\":\"工作台不能查看到\",\"priority\":0},\"children\":[]}]}]},{\"data\":{\"created\":1663671892545,\"progress\":9,\"id\":\"cn16gsfaf340\",\"text\":\"范德萨范德萨\"},\"children\":[{\"data\":{\"created\":1663724574915,\"id\":\"cn1p52ddblk0\",\"text\":\"知道了\"},\"children\":[]}]},{\"data\":{\"created\":1663672785550,\"progress\":5,\"id\":\"cn16s6ny7lk0\",\"text\":\"呢能\"},\"children\":[]},{\"data\":{\"created\":1663672788421,\"id\":\"cn16s7zft9c0\",\"text\":\"2\"},\"children\":[{\"data\":{\"created\":1663724637530,\"progress\":9,\"id\":\"cn1p5v4wic00\",\"text\":\"哈哈哈哈\"},\"children\":[]}]},{\"data\":{\"created\":1663725392976,\"id\":\"cn1pfi6lqy80\",\"text\":\"nihao \"},\"children\":[{\"data\":{\"created\":1663725543157,\"id\":\"cn1phf6bll40\",\"text\":\"我就是想试试\"},\"children\":[]}]}]},\"theme\":\"fresh-blue\",\"version\":\"1.4.43\",\"base\":34}";
        JSONObject caseObj = JSONObject.parseObject(caseContent);
        JSONObject root = caseObj.getJSONObject("root");
        System.out.println("原始root = " + root);
        convertNotoProgress(root);
        System.out.println("处理后root = " + root);
        convertProgressToNot(root);
        System.out.println("还原root = " + root);
    }

    @Test
    public void test2(){
        String caseContent = "{\"template\":\"right\",\"root\":{\"data\":{\"image\":\"\",\"created\":\"1634006790476\",\"id\":\"0ggnje1shnb52bmu39q8raucic\",\"imageSize\":{},\"text\":\"20201014发布工作台相关\",\"priority\":0},\"children\":[{\"data\":{\"image\":\"\",\"created\":\"1634006790474\",\"id\":\"1fhbasksbo0222f6tfbp6tg7nt\",\"imageSize\":{},\"text\":\"视频问诊异常处理\",\"priority\":0},\"children\":[{\"data\":{\"image\":\"\",\"created\":\"1634006790472\",\"id\":\"737ljc5esldcka4bde6gg1hasi\",\"imageSize\":{},\"text\":\"问诊完成后\",\"priority\":0},\"children\":[{\"data\":{\"image\":\"\",\"created\":\"1634006790472\",\"progress\":1,\"id\":\"3q7so0rtlp9dafbu9l5hnr8e1s\",\"imageSize\":{},\"text\":\"工作台关闭视频问诊对话框\",\"priority\":0},\"children\":[]}]},{\"data\":{\"image\":\"\",\"created\":\"1634006790472\",\"id\":\"72joluq93tdml2ssg4qj8poium\",\"imageSize\":{},\"text\":\"视频问诊信息素\",\"priority\":0},\"children\":[{\"data\":{\"image\":\"\",\"created\":\"1634006790472\",\"progress\":1,\"id\":\"6876m1ra3upu0dj9fvfna8ijh2\",\"imageSize\":{},\"text\":\"子主题 1\",\"priority\":0},\"children\":[]}]},{\"data\":{\"image\":\"\",\"created\":\"1634006790474\",\"progress\":1,\"id\":\"71ak6r62kjp9j6rlgvk1k6klqo\",\"imageSize\":{},\"text\":\"工作台新增异常结束场景\",\"priority\":0},\"children\":[{\"data\":{\"image\":\"\",\"created\":\"1634006790473\",\"progress\":9,\"id\":\"461j63btk9d3o0invpc5uc7ug2\",\"imageSize\":{},\"text\":\"点击异常挂断\",\"priority\":0},\"children\":[{\"data\":{\"image\":\"\",\"created\":\"1634006790473\",\"progress\":5,\"id\":\"46bei8d2mga97lvouglrkua0ll\",\"imageSize\":{},\"text\":\"返还权益\",\"priority\":0},\"children\":[]}]}]}]},{\"data\":{\"image\":\"\",\"created\":\"1634006790474\",\"id\":\"1ntsmjtkbjia4f1uaeci7htiim\",\"imageSize\":{},\"text\":\"权益作废\",\"priority\":0},\"children\":[{\"data\":{\"image\":\"\",\"created\":\"1634006790474\",\"progress\":9,\"id\":\"6ev6p3p2a7ts8njhc7gmuk8he3\",\"imageSize\":{},\"text\":\"用户拉人入群不会收到话术和邀请家人卡片\",\"priority\":0},\"children\":[]}]},{\"data\":{\"image\":\"\",\"created\":\"1634006790475\",\"id\":\"7glf6fub1lahdaqcaunjb4lfgl\",\"imageSize\":{},\"text\":\"删除副权益人\",\"priority\":0},\"children\":[{\"data\":{\"image\":\"\",\"created\":\"1634006790475\",\"id\":\"484breebnnfa127hshrsoba517\",\"imageSize\":{},\"text\":\"副权益人被删除后\",\"priority\":0},\"children\":[{\"data\":{\"image\":\"\",\"created\":\"1634006790475\",\"progress\":4,\"id\":\"6qrnvsj0m4g4j92ii1qkvhml1m\",\"imageSize\":{},\"text\":\"工作台不能查看到\",\"priority\":0},\"children\":[]}]}]},{\"data\":{\"created\":1663671892545,\"progress\":9,\"id\":\"cn16gsfaf340\",\"text\":\"范德萨范德萨\"},\"children\":[{\"data\":{\"created\":1663724574915,\"id\":\"cn1p52ddblk0\",\"text\":\"知道了\"},\"children\":[]}]},{\"data\":{\"created\":1663672785550,\"progress\":5,\"id\":\"cn16s6ny7lk0\",\"text\":\"呢能\"},\"children\":[]},{\"data\":{\"created\":1663672788421,\"id\":\"cn16s7zft9c0\",\"text\":\"2\"},\"children\":[{\"data\":{\"created\":1663724637530,\"progress\":9,\"id\":\"cn1p5v4wic00\",\"text\":\"哈哈哈哈\"},\"children\":[]}]},{\"data\":{\"created\":1663725392976,\"id\":\"cn1pfi6lqy80\",\"text\":\"nihao \"},\"children\":[{\"data\":{\"created\":1663725543157,\"id\":\"cn1phf6bll40\",\"text\":\"我就是想试试\"},\"children\":[]}]}]},\"theme\":\"fresh-blue\",\"version\":\"1.4.43\",\"base\":34}";
        JSONObject caseObj = JSONObject.parseObject(caseContent);

        JSONObject rootObj = caseObj.getJSONObject("root");
        System.out.println("rootObj.getJSONArray(\"children\") = " + rootObj.getJSONArray("children"));
        rootObj.getJSONArray("children").removeIf(item -> {
            String arrayString = ((JSONObject) item).getJSONObject("data").getString("id");
            if (arrayString.equals("1fhbasksbo0222f6tfbp6tg7nt")){
                return true;
            }
            return false;
        });
        System.out.println(" = = = = = = = = = = = = = = == = = = = = == = = = = = = = = = = = ==");
        System.out.println("删除后rootObj.getJSONArray(\"children\") = " + rootObj.getJSONArray("children"));
        //JSONArray children = rootObj.getJSONArray("children");
        rootObj.getJSONArray("children").clear();
        System.out.println(" = = = = = = = = = = = = = = == = = = = = == = = = = = = = = = = = ==");
        System.out.println("rootObj.getJSONArray(\"children\") = " + rootObj.getJSONArray("children"));
        System.out.println("rootObj根节点删除了children = " + rootObj);

        System.out.println("caseObj在rootObj根节点删除了children = " + caseObj);
    }


}
