package com.xiaoju.framework.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.xiaoju.framework.constants.enums.ProgressEnum;
import com.xiaoju.framework.entity.dto.CountsCollect;
import com.xiaoju.framework.entity.request.cases.FileImportReq;
import com.xiaoju.framework.entity.xmind.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.entity.ContentType;
import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;


/**
 * 树 - 数据结构处理类
 * xmind 和 文件夹都用到了
 *
 * @author didi
 * @date 2020/11/26
 */
public class TreeUtil {

    /**
     * 常量
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TreeUtil.class);


    // 剥离出progress的内容
    public static JSONObject parse(String caseContent) {
        JSONObject retContent = new JSONObject();
        CaseContent content = JSONObject.parseObject(caseContent, CaseContent.class); // 将casecontent的内容解析为CaseContent.class对象并返回
        CaseCount count = caseDFS(content.getRoot());
        JSONObject caseObj = JSON.parseObject(caseContent);

        retContent.put("progress", count.getProgress());
        retContent.put("content", caseObj);
        retContent.put("passCount", count.getPassCount());
        retContent.put("totalCount", count.getTotal());
        retContent.put("successCount", count.getSuccess());
        retContent.put("blockCount", count.getBlock());
        retContent.put("failCount", count.getFail());
        retContent.put("ignoreCount", count.getIgnore());

        return retContent;
    }

    /**
     * ******深度递归，获取每个用例的具体内容，读出所有的计数**********
     * 根据一份测试用例，递归获取其中所有底部节点的用例执行情况
     * 分为两种情况：
     * ①当前节点有子节点
     *  <1>如果当前节点状态为1、5、9，那么值收集下游节点的个数total和，然后变成自己对应的状态个数+=childTotalSum,total++
     *  <2>如果节点为4，则忽略
     *  <3>如果节点为null，则total++
     * ②当前节点无子节点
     *  计数体对应的状态数++,total++
     *
     * @param rootData 当前用例节点
     * @return 记录对象体
     */
    public static CaseCount caseDFS(RootData rootData) {
        CaseCount currCount = new CaseCount();

        DataObj currNode = rootData.getData();
        List<RootData> childNodes = rootData.getChildren();

        if (!CollectionUtils.isEmpty(childNodes)) {

            if (currNode.getProgress() != null) {
                int num = 0;
                for (RootData childNode : childNodes) {
                    CaseCount cc = caseDFS(childNode);
                    num += cc.getTotal();
                    currCount.addAllProgress(cc.getProgress());
                }
                switch (ProgressEnum.findEnumByProgress(currNode.getProgress())) {
                    case BLOCK:
                        currCount.combineBlock(num);
                        currCount.addProgress(currNode.getId(), currNode.getProgressStr());
                        break;
                    case SUCCESS:
                        currCount.combineSuccess(num);
                        currCount.addProgress(currNode.getId(), currNode.getProgressStr());
                        break;
                    case FAIL:
                        currCount.combineFail(num);
                        currCount.addProgress(currNode.getId(), currNode.getProgressStr());
                        break;
                    case IGNORE:
                        currCount.combineIgnore(num);
                        currCount.addProgress(currNode.getId(), currNode.getProgressStr());
                        break;
                    default:
                        currCount.addTotal(num);
                }
            } else {
                for (RootData childNode : childNodes) {
                    currCount.cover(caseDFS(childNode));
                }
            }
        } else {
            // 先把超链接、备注都加进来
            // 最底部的节点，没有任何子节点
            switch (ProgressEnum.findEnumByProgress(currNode.getProgress())) {
                case BLOCK:
                    currCount.addBlock();
                    currCount.addProgress(currNode.getId(), currNode.getProgressStr());
                    break;
                case SUCCESS:
                    currCount.addSuccess();
                    currCount.addProgress(currNode.getId(), currNode.getProgressStr());
                    break;
                case FAIL:
                    currCount.addFail();
                    currCount.addProgress(currNode.getId(), currNode.getProgressStr());
                    break;
                case IGNORE:
                    currCount.addIgnore();
                    currCount.addProgress(currNode.getId(), currNode.getProgressStr());
                    break;
                default:
                    currCount.addTotal();
            }
        }
        return currCount;
    }

    public static void caseDFSValidate(JsonNode rootData) {
        if (rootData == null) return;
        JsonNode currNode = rootData.get("data");
        if (currNode.has("resource")) {
            JsonNode resourceNode = currNode.get("resource");
            if (resourceNode.isNull()) {
                ((ObjectNode) currNode).remove("resource");
                LOGGER.info("remove resource is null node. " + currNode.get("text"));
            } else {
                ArrayNode resources = ((ArrayNode) resourceNode);
                int resourcesSize = resources.size();
                for (int i = 0; i < resourcesSize; i++) {
                    if (resources.get(i).isNull()) {

                        resources.remove(i);
                        i --;
                        resourcesSize --;
                        LOGGER.info("remove resource contain null node. " + currNode.get("text"));
                    }
                }
            }
        }

        JsonNode childNodes = rootData.get("children");

        if(childNodes == null || childNodes.size() == 0) {
            return ;
        }
        for (int i = 0; i < childNodes.size(); i++) {
            caseDFSValidate(childNodes.get(i));
        }

    }

    //获取所有的叶子节点
    public static List<String> getAllLeafNode(RootData rootData){
        List<String> nodes = new ArrayList<>();
        List<RootData> childrenNodes = rootData.getChildren();
        if (CollectionUtils.isEmpty(childrenNodes)){
            DataObj currNode = rootData.getData();
            nodes.add(currNode.getId());
        }else {
            for (RootData data: childrenNodes) {
                //递归获取子节点的叶子节点
                List<String> dataLeafs = getAllLeafNode(data);
                nodes.addAll(dataLeafs);
            }
        }
        return nodes;
    }


    public static Integer getLeafNodeCount(String content){
        CaseContent caseContent = JSON.parseObject(content, CaseContent.class);
        RootData rootData = caseContent.getRoot();
        int size = getAllLeafNode(rootData).size();
        return size;
    }

    // 获取指定优先级的内容，入参为root节点
    public static void getPriority(Stack<JSONObject> stackCheck, Stack<IntCount> iCheck, JSONObject parent, List<String> priorities) {
        JSONArray children = parent.getJSONArray("children");
        IntCount i = new IntCount(0);

        for (; i.get() < children.size(); ) {

            JSONObject obj = (JSONObject) children.get(i.get());
            i.add();
            if (obj.getJSONObject("data").containsKey("priority") &&
                    obj.getJSONObject("data").getInteger("priority") != null &&
                    isPriorityIn(obj.getJSONObject("data").getInteger("priority"), priorities)) {
                continue;
            } else {
                if (Objects.isNull(obj.getJSONArray("children")) || obj.getJSONArray("children").size() == 0) { // 当前是叶子结点
                    children.remove(obj);
                    i.del();
                    traverseCut(stackCheck, iCheck);
                } else {
                    stackCheck.push(obj);
                    iCheck.push(i);
                    getPriority(stackCheck, iCheck, obj, priorities);
                }
            }
        }
    }

    //获取指定标签case
    public static boolean getChosenCase(JSONObject root, Set<String> tags, String field) {
        if (root == null) return false;

        boolean hasTags = false;
        //筛选标签
        if (field.equals("resource")) {
            JSONArray objects = root.getJSONObject("data").getJSONArray("resource");
            if (objects != null) {
                for (Object o : objects) {
                    hasTags = hasTags || tags.contains(o);
                }
            }
            if (hasTags) return true;
        } else if (field.equals("priority")) { //筛选优先级
            String priority = root.getJSONObject("data").getString("priority");
            if (tags.contains(priority)) return true;
        }
        JSONArray children = root.getJSONArray("children");
        Iterator<Object> iterator = children.iterator();
        while (iterator.hasNext()) {
            JSONObject child = (JSONObject) iterator.next();
            if (!getChosenCase(child, tags, field)) iterator.remove();
        }
        return children.size() != 0;

    }

    //获取节点个数以及标签信息
    public static Integer getCaseNum(JSONObject root, Set<String> set) {
        if (root == null) return 0;
        int res = 0;

        JSONArray resource = root.getJSONObject("data").getJSONArray("resource");

        if (resource != null) {
            for (Object o : resource) {
                set.add((String) o);
            }
        }

        JSONArray children = root.getJSONArray("children");
        if(children.size() == 0) return 1;
        for (Object child : children) {
            res += getCaseNum((JSONObject) child, set);
        }


        return res;
    }

    //获取指定节点路径
    // 返回值jsonNode为空&path size为0，表示未找到；jsonNode非空，path size为空，表示当前找到根节点；
    public static boolean getNodePath(JsonNode root, String nodeId, List<Integer> path, Map<String, JsonNode> relatedNode) {
        if (root == null) return false;

        String currentid = root.get("data").get("id").asText();

        if (currentid.equals(nodeId)) {
            relatedNode.put("objectNode", root);
            return true;
        }

        JsonNode children = root.get("children");

        if(children.size() == 0) {
            return false;
        }

        for (int i = 0; i < children.size(); i++) {
            path.add(i);
            boolean ret = getNodePath(children.get(i), nodeId, path, relatedNode);
            if (ret) {
                System.out.println("找到了");
                if (!relatedNode.containsKey("parent")) {
                    relatedNode.put("parentNode", root);
                }
                return true;
            } else {
                path.remove(path.size()-1);
            }
        }

        return false;
    }

    //获取优先级为0的内容，入参为root节点
    public static void getPriority0(Stack<JSONObject> stackCheck, Stack<IntCount> iCheck, JSONObject parent) {
        JSONArray children = parent.getJSONArray("children");
        IntCount i = new IntCount(0);

        for (; i.get() < children.size(); ) {

            JSONObject obj = (JSONObject) children.get(i.get());
            i.add();
            if (obj.getJSONObject("data").containsKey("priority") && obj.getJSONObject("data").getLong("priority") == 1L) {
                continue;
            } else {
                if (obj.getJSONArray("children").size() == 0) { // 当前是叶子结点
                    children.remove(obj);
                    i.del();
                    traverseCut(stackCheck, iCheck);
                } else {
                    stackCheck.push(obj);
                    iCheck.push(i);
                    getPriority0(stackCheck, iCheck, obj);
                }
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

    public static void traverseCut(Stack<JSONObject> stackCheck, Stack<IntCount> iCheck) {
        int size = stackCheck.size();
        for (int i = 0; i < size - 1; i++) {
            JSONObject top = stackCheck.peek();
            if (top.getJSONArray("children").size() == 0) {
                stackCheck.pop();
                stackCheck.peek().getJSONArray("children").remove(top);
                iCheck.pop().del();
            } else {
                break;
            }
        }
    }

    /**
     * 将执行结果合并到用例中
     *
     * @param caseContent 用例内容
     * @param execContent 执行内容
     */
    public static void mergeExecRecord(JSONObject caseContent, JSONObject execContent, IntCount execCount) {
        String srcId = caseContent.getJSONObject("data").getString("id");
        if (execContent.containsKey(srcId)) {
            caseContent.getJSONObject("data").put("progress", execContent.getLong(srcId));
            //execCount.del();
        }
        if (null != caseContent && null != caseContent.getJSONArray("children")) {
            for (Object o : caseContent.getJSONArray("children")) {
                if (execCount.get() != 0) {
                    mergeExecRecord(((JSONObject) o), execContent, execCount);
                }
            }
        }
    }

    // 导出json内容到xml
    public static void exportDataToXml(JSONArray children, Element rootTopic, String path){
        if(children.size() == 0)
            return;

        Document document = rootTopic.getDocument();
        LOGGER.info("rootTopic中的内容为： " + rootTopic);
        LOGGER.info("document中的内容为：" + document);
        Element children1 = rootTopic.addElement("children");
        Element topics = children1.addElement("topics").addAttribute("type","attached");
        for (Object o : children) {
            JSONObject dataObj = ((JSONObject) o).getJSONObject("data");


            Element topic = topics.addElement("topic")
                    .addAttribute("id",dataObj.getString("id"))
                    .addAttribute("modified-by","didi")
                    .addAttribute("timestamp",dataObj.getString("created"))
                    .addAttribute("imageTitle", dataObj.getString("imageTitle"));

            JSONObject dataObj1 = dataObj.getJSONObject("imageSize");
            String picPath = dataObj.getString("image");
            if(picPath != null && picPath.length() != 0){
                String targetPath = path  + "/attachments";

                // 创建一个新的文件夹
                File file = new File(targetPath);
                if(!file.isDirectory()){
                    file.mkdir();
                }
                try{
                    String[] strs = picPath.split("/");
                    int size = strs.length;
                    String fileName = strs[size - 1];
                    LOGGER.info("picPath路径为：" + picPath);
                    LOGGER.info("outfile的内容为：" + file + "/" + fileName);

                    if(dataObj1 != null && dataObj1.getString("width") != null){
                        LOGGER.info("topic1的内容为：" + topic);
                        LOGGER.info("dataonj1中有内容, 其中width：" + dataObj1.getString("width") + "  ，height：" + dataObj1.getString("height"));
                        Element imageSize = topic.addElement("xhtml:img")
                                .addAttribute("svg:height", dataObj1.getString("height"))
                                .addAttribute("svg:width", dataObj1.getString("width"))
                                .addAttribute("xhtml:src", "xap:attachments/" + fileName);

                    }

                    FileOutputStream outFile = new FileOutputStream(file + "/" + fileName);
                    URL httpUrl=new URL(picPath);
                    HttpURLConnection conn=(HttpURLConnection) httpUrl.openConnection();
                    //以Post方式提交表单，默认get方式
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    // post方式不能使用缓存
                    conn.setUseCaches(false);
                    //连接指定的资源
                    conn.connect();
                    //获取网络输入流
                    InputStream inputStream=conn.getInputStream();
                    BufferedInputStream bis = new BufferedInputStream(inputStream);
                    byte b [] = new byte[1024];
                    int len = 0;
                    while((len=bis.read(b))!=-1){
                        outFile.write(b, 0, len);
                    }
                    LOGGER.info("下载完成...");
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }

            Element title = topic.addElement("title");
            String text = dataObj.getString("text");
            if (!StringUtils.isEmpty(text)) {
                text = StringEscapeUtils.escapeXml11(text);
            } else {
                text = "";
            }
            title.setText(text);

            String priority = getPriorityByJson(dataObj);
            if(priority != null && !priority.equals("")){
                Element marker_refs = topic.addElement("marker-refs");
                marker_refs.addElement("marker-ref")
                        .addAttribute("marker-id",priority);
            }
            JSONArray childChildren = ((JSONObject) o).getJSONArray("children");
            if (childChildren != null && childChildren.size() > 0) {
                exportDataToXml(childChildren, topic, path);
            }
        }
    }

    public static void importDataByJson(JSONArray children, JSONObject rootTopic, String fileName, HttpServletRequest requests, String uploadPath) throws IOException {
        JSONObject rootObj = new JSONObject();
        JSONObject dataObj = new JSONObject();
        JSONArray childrenNext = new JSONArray();
        dataObj.put("text", rootTopic.getString("title"));
        dataObj.put("created", System.currentTimeMillis());
        dataObj.put("id", rootTopic.getString("id"));
        if(rootTopic.containsKey("image")){ // 添加imagesize属性
            // 需要将图片传到云空间中，然后将返回的链接导入
            Map<String, String> imageSize = new HashMap<>();
            // todo: 此处直接写死的方式存在问题
            imageSize.put("width", "400");
            imageSize.put("height", "184");
            String image = "";
            String picPath = "";
            String path = rootTopic.getJSONObject("image").getString("src");
            String[] strs = path.split("/");
            int len = strs.length;
            image = strs[len-1]; // 此时image为图片所在的本地位置
            // 将文件传入到temp文件下，因此需要将文件进行转换，将file文件类型转化为MultipartFile类型，然后进行上传
            File file = new File(fileName + File.separator + image);
            FileInputStream fileInputStream = new FileInputStream(file);

            MultipartFile multipartFile = new MockMultipartFile(file.getName(), file.getName(),
                    ContentType.APPLICATION_OCTET_STREAM.toString(), fileInputStream);

            // 将MultipartFile文件进行上传
            JSONObject ret = new JSONObject();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd/");
            String format = sdf.format(new Date());
            File folder = new File(uploadPath + format);// 文件夹的名字
            if (!folder.isDirectory()) { // 如果文件夹为空，则新建文件夹
                folder.mkdirs();
            }
            // 对上传的文件重命名，避免文件重名
            String oldName = multipartFile.getOriginalFilename(); // 获取文件的名字
            String newName = UUID.randomUUID().toString()
                    + oldName.substring(oldName.lastIndexOf("."), oldName.length()); // 生成新的随机的文件名字
            File newFile = new File(folder, newName);
            LOGGER.info("newFile的名字为" + newFile);
            try {
                multipartFile.transferTo(newFile);
                // 返回上传文件的访问路径
                // request.getScheme()可获取请求的协议名，request.getServerName()可获取请求的域名，request.getServerPort()可获取请求的端口号
                String filePath = requests.getScheme() + "://" + requests.getServerName()
                        + ":" + requests.getServerPort() + "/" + format + newName;
                LOGGER.info("filepath的路径为：" + filePath);
                picPath = filePath;
                JSONArray datas = new JSONArray();
                JSONObject data = new JSONObject();
                data.put("url", filePath);
                ret.put("success", 1);
                datas.add(data);
                ret.put("data", datas);
            } catch (IOException err) {
                LOGGER.error("上传文件失败, 请重试。", err);
                ret.put("success", 0);
                ret.put("data", "");
            }
            dataObj.put("image", picPath);
            dataObj.put("imageSize", imageSize);
        }

        Integer priority = getPriorityByJsonArray(rootTopic.getJSONArray("markers"));

        if(priority != 0)
        {
            dataObj.put("priority",priority);
        }
        rootObj.put("data", dataObj);
        rootObj.put("children", childrenNext);
        if (children != null) {
            children.add(rootObj);
        }
        if (rootTopic.containsKey("children") && rootTopic.getJSONObject("children").containsKey("attached")) {
            JSONArray jsonArray = rootTopic.getJSONObject("children").getJSONArray("attached");
            for (int i = 0; i < jsonArray.size(); i++) {
                importDataByJson(childrenNext, (JSONObject) jsonArray.get(i), fileName, requests, uploadPath);
            }
        }
    }


    //导入xml内容
    public static JSONArray importDataByXml(FileImportReq request, Element e, String fileName, HttpServletRequest requests, String uploadPath) throws IOException {
        JSONArray jsonArray = new JSONArray();
        List<Element> elementList = e.elements();
        if(elementList.size() == 0)
            return jsonArray;
        for(Element element1:elementList) {
            if(element1.getName().equalsIgnoreCase("topic"))
            {
                JSONArray childrenNext = new JSONArray();
                JSONObject root = new JSONObject();
                JSONObject dataObj = new JSONObject();
                List<Element> newList = element1.elements();
                Map<String, String> imageSize = new HashMap<>();
                String text = "";
                String picPath = "";
                Integer priorityId = 0;
                String created = element1.attributeValue("timestamp");
                String id = element1.attributeValue("id");

                for (Element element : newList) {
                    // 获取xml里面的图片大小信息
                    if(element.getName().equalsIgnoreCase("img")){ // 添加imagesize属性
                        // 需要将图片传到云空间中，然后将返回的链接导入
                        LOGGER.info("xhtml:img可以使用，其中element中的内容为：" + element);

                        String path = element.attributeValue("src");

                        // 将文件传入到temp文件下，因此需要将文件进行转换，将file文件类型转化为MultipartFile类型，然后进行上传
                        File file = new File(fileName + path.split(":")[1]);
                        try {
                            if (StringUtils.isEmpty(element.attributeValue("width")) || StringUtils.isEmpty(element.attributeValue("height"))) {
                                BufferedImage sourceImg = ImageIO.read(new FileInputStream(file));
                                imageSize.put("width", String.valueOf(sourceImg.getWidth()));
                                imageSize.put("height", String.valueOf(sourceImg.getHeight()));
                            } else {
                                imageSize.put("width", element.attributeValue("width"));
                                imageSize.put("height", element.attributeValue("height"));
                            }

                            MultipartFile multipartFile = new MockMultipartFile(file.getName(), new FileInputStream(file));

                            String fileUrlPath = FileUtil.fileUpload(uploadPath, multipartFile);

                            // 返回上传文件的访问路径
                            // request.getScheme()可获取请求的协议名，request.getServerName()可获取请求的域名，request.getServerPort()可获取请求的端口号
                            String filePath = requests.getScheme() + "://" + requests.getServerName()
                                    + ":" + requests.getServerPort() + "/" + fileUrlPath;
                            LOGGER.info("filepath的路径为：" + filePath);
                            picPath = filePath;

                        } catch (Exception err) {
                            LOGGER.error("图片上传文件失败, 请重试。", err);
                        }
                    }

                    // 获取xml里面中的图片importDataByXml1

                    else if (element.getName().equalsIgnoreCase("title")) {
                        //标题
                        text = element.getText();
                    }else if (element.getName().equalsIgnoreCase("marker-refs")) {
                        // 优先级
                        priorityId =  getPriorityByElement(element);
                    }else if (element.getName().equalsIgnoreCase("children")) {
                        //子节点
                        List<Element> elementList1 = element.elements();
                        for(Element childEle:elementList1)
                        {
                            if(childEle.getName().equalsIgnoreCase("topics"))
                            {
                                JSONArray jsonArray1 = importDataByXml(request, childEle, fileName, requests, uploadPath);
                                if(jsonArray1.size()>0){
                                    childrenNext.addAll(jsonArray1);
                                }
                            }
                        }
                    } else {
                        continue;
                    }
                }

                dataObj.put("created", created);
                dataObj.put("id", id);
                dataObj.put("image", picPath);
                dataObj.put("imageSize", imageSize);
                dataObj.put("text", text);
                dataObj.put("priority", priorityId);
                root.put("data",dataObj);
                if(childrenNext.size() != 0) {
                    root.put("children",childrenNext);
                }
                jsonArray.add(root);
            }
        }
        return jsonArray;

    }

     //根据xml文件获取优先级
     private static Integer getPriorityByElement(Element element)
     {
         Integer priorityId = 0;
         Map<String, Integer> priorityIds = getAllPriority();
         List<Element> markers = element.elements();
         if (markers != null && markers.size() > 0) {
             for (Element mark : markers) {
                 String markId = mark.attributeValue("marker-id");
                 if (priorityIds.containsKey(markId)) {
                     priorityId = priorityIds.get(markId);
                 }
             }
         }
         return priorityId;
     }

    //根据content.json文件获取优先级
    private static Integer getPriorityByJsonArray(JSONArray markers)
    {
        Integer priorityId = 0;
        Map<String, Integer> priorityIds = getAllPriority();
        if (markers != null && markers.size() > 0) {
            for (int i = 0; i < markers.size(); i++) {
                String markerId = markers.getJSONObject(i).getString("markerId");
                if (priorityIds.containsKey(markerId)) {
                    priorityId = priorityIds.get(markerId);
                }
            }
        }
        return priorityId;
    }


    //根据case-server  json获取xml优先级
    private static String getPriorityByJson(JSONObject jsonObject)
    {
        Integer priority = 0;
        priority = jsonObject.getInteger("priority");
        String topicPriority = "";
        if(priority != null && priority != 0){
            if(priority.equals(3)){
                topicPriority = "priority-3";
            }else
            {
                Map<String, Integer> priorityIds = getAllPriority();
                for (Map.Entry<String, Integer> entry : priorityIds.entrySet()) {
                    //如果value和key对应的value相同 并且 key不在list中
                    if(priority.equals(entry.getValue())){
                        topicPriority=entry.getKey();
                        break;
                    }
                }
            }
        }
        return  topicPriority;
    }

    //获取所有优先级
     private static Map<String, Integer> getAllPriority(){
         Map<String, Integer> priorityIds = new HashMap<>();
         priorityIds.put("priority-1", 1);
         priorityIds.put("priority-2", 2);
         priorityIds.put("priority-3", 3);
         priorityIds.put("priority-4", 3);
         priorityIds.put("priority-5", 3);
         priorityIds.put("priority-6", 3);
         priorityIds.put("priority-7", 3);
         priorityIds.put("priority-8", 3);
         priorityIds.put("priority-9", 3);
         return priorityIds;
     }

     public static CountsCollect getProgressCountsCollect(String caseContent){
         CountsCollect countsCollect = new CountsCollect();
        if (org.apache.commons.lang3.StringUtils.isEmpty(caseContent)){
            return countsCollect;
        }
        JSONObject jsonObject = JSON.parseObject(caseContent);
        //LOGGER.info("jsonObject 计算的值:{}",jsonObject.toJSONString());
        //LOGGER.info("jsonObject.values()的值:{}",jsonObject.values());
         List<Integer> collect = jsonObject.values().stream().map(t -> {
             return Integer.parseInt(String.valueOf(t));
         }).collect(Collectors.toList());
         //LOGGER.info("collect的值:{}",collect);
         //if (p === '4') label = '不执行'
         //if (p === '5') label = '阻塞'
         //if (p === '9') label = '通过'
         //if (p === '1') label = '不通过'

         for (Integer value: collect) {
             switch (value){
                 case 9:{
                     countsCollect.addSuccessCount();
                     break;
                 }
                 case 1:{
                     countsCollect.addFailCount();
                     break;
                 }
                 case 5:{
                     countsCollect.addBlockCount();
                     break;
                 }
                 case 4:{
                     countsCollect.addIgnoreCount();
                     break;
                 }
                 default:{
                     break;
                 }
             }
         }
        return countsCollect;

     }

     public static List<List<RootData>> result = new ArrayList();
     //把脑图内容转成list
     public static List<List<RootData>> parseRootDataToList(RootData rootData){

        List<RootData> caseList = new ArrayList();
        getRootDataPath(rootData,caseList);
        return result;
     }

     public static void getRootDataPath(RootData rootData, List list){
         list.add(rootData);
         if (rootData.getChildren().size()>0){
             rootData.getChildren().stream().forEach(item -> {
                 List newList = new ArrayList();
                 newList.addAll(list);
                 getRootDataPath(item,newList);
             });
         }else{
             result.add(list);
         }
     }


     public static boolean isInProgresss(JSONObject jsonObject,List<Integer> progresss){
         AtomicBoolean flag = new AtomicBoolean(false);
         String nodeString = jsonObject.toJSONString();
         //如果是空数据的时候，只要node中不包含progress
         if (progresss.size() == 0){
             if(!nodeString.contains("progress")){
                 flag.set(true);
             }
         }else {
             progresss.stream().map(progress -> "\"progress\":"+progress).forEach(
                     stringProgress ->{
                         if(nodeString.contains(stringProgress)){
                             flag.set(true);
                         }
                     }
             );
         }

        return flag.get();
     }

     //筛选用例
     public static String filterProgress(String caseContent, List<Integer> progresss){
         //传入null，就是不过滤
         if (Objects.isNull(progresss)){
             return caseContent;
         }

         JSONObject jsonObject = JSONObject.parseObject(caseContent);
         //如果要过滤的内容为空数组，就仅仅过滤没执行的用例
         if (progresss.size() == 0){
            return caseContent;
         }else {
             if (isInProgresss(jsonObject,progresss)){
                 JSONArray childern = jsonObject.getJSONArray("childern");


             }
         }


         return null;
     }

     public static void main(String args[]) {
         /*String str = "{\"id\":\"97f60c4b-391f-4cbd-baa8-2067346a9b3b\",\"resource\":[null,\"123\",null,\"xxx\", null]}";
         ObjectMapper jsonMapper = new ObjectMapper();
         try {
             JsonNode node = jsonMapper.readTree(str);
             ArrayNode resources = ((ArrayNode) node.get("resource"));
             int resourcesSize = resources.size();
             for (int i = 0; i < resourcesSize; i++) {
                 if (resources.get(i).isNull()) {
                     resources.remove(i);
                     i --;
                     resourcesSize --;
                 }
             }
             System.out.println(resources);
         } catch (ExTrtion e) {

         }*/
         //String caseContent = "{\"root\":{\"data\":{\"id\":\"bv8nxhi3c800\",\"created\":1562059643204,\"text\":\"工作台用例test\"},\"children\":[{\"data\":{\"id\":\"cnvqy6rf6lc0\",\"created\":1666776476843,\"text\":\"及发放\"},\"children\":[]},{\"data\":{\"id\":\"cnvqy8rfnq80\",\"created\":1666776481197,\"text\":\"分支主题\"},\"children\":[]},{\"data\":{\"id\":\"cnvqyf4za080\",\"created\":1666776495077,\"text\":\"发放地方\"},\"children\":[]}]},\"template\":\"right\",\"theme\":\"classic-compact\",\"version\":\"1.4.43\",\"base\":9,\"right\":1}";
         //String caseContent = "{\"root\":{\"data\":{\"image\":\"\",\"created\":\"1665911754549\",\"id\":\"6jvmd554i8rfncm1ult7tjqhp9\",\"imageSize\":{},\"text\":\"【10.20】复诊单开单及问诊记录中记录\",\"priority\":0},\"children\":[{\"data\":{\"image\":\"\",\"created\":\"1665906770479\",\"id\":\"266kgm8gt6j51upd08p5nq04sq\",\"imageSize\":{},\"text\":\"两天内就诊人A结束过一次问诊并关闭问诊单\",\"priority\":0},\"children\":[{\"data\":{\"image\":\"\",\"created\":\"1665906804746\",\"id\":\"2f4rahop5on7pc8tquhubbvmcp\",\"imageSize\":{},\"text\":\"健康咨询关单\",\"priority\":0},\"children\":[{\"data\":{\"image\":\"\",\"created\":\"1665906804682\",\"id\":\"7i9hal4k32sjnh83ns1ktnel7d\",\"imageSize\":{},\"text\":\"医生主动开单\",\"priority\":0},\"children\":[{\"data\":{\"image\":\"\",\"created\":\"1665906804681\",\"id\":\"240bf7s5sldn1olfad6v43mno0\",\"imageSize\":{},\"text\":\"飘窗中伴随症状，诊疗经过有内容，其他字段无内容\",\"priority\":0},\"children\":[{\"data\":{\"image\":\"\",\"created\":\"1665906804681\",\"id\":\"43m2vc0trv4iquot6a0blqb4t4\",\"imageSize\":{},\"text\":\"选择就诊人A\",\"priority\":0},\"children\":[{\"data\":{\"image\":\"\",\"created\":\"1665906804681\",\"progress\":9,\"id\":\"084h2uh15mmp5cmchc4uda3u24\",\"imageSize\":{},\"text\":\"就诊人旁边有复诊标\",\"priority\":2},\"children\":[]}]},{\"data\":{\"image\":\"\",\"created\":\"1665906804681\",\"id\":\"6au8np5342c0mtlpajd3jgcr3m\",\"imageSize\":{},\"text\":\"选择就诊人B\",\"priority\":0},\"children\":[{\"data\":{\"image\":\"\",\"created\":\"1665906804681\",\"progress\":9,\"id\":\"1u22v8jvk2kln5rasphfug6out\",\"imageSize\":{},\"text\":\"飘窗中内容不变化\",\"priority\":2},\"children\":[]}]},{\"data\":{\"image\":\"\",\"created\":\"1665906804681\",\"id\":\"1o0jov23pijjaf8qqfm0sihtbc\",\"imageSize\":{},\"text\":\"没选就诊人\",\"priority\":0},\"children\":[{\"data\":{\"image\":\"\",\"created\":\"1665906804681\",\"progress\":9,\"id\":\"7cquqh1l8ec22pruokjntreadk\",\"imageSize\":{},\"text\":\"无复诊标\",\"priority\":2},\"children\":[]}]}]}]}]}]},{\"data\":{\"image\":\"\",\"created\":\"1665907134566\",\"id\":\"5jpu469e7e9d52jre5opjqqoop\",\"imageSize\":{},\"text\":\"两天内就诊人A和B都结束过一次问诊并关闭问诊单\",\"priority\":0},\"children\":[{\"data\":{\"image\":\"\",\"created\":\"1665907201445\",\"id\":\"7rc2bdmrl52d3go4d8cd54elhe\",\"imageSize\":{},\"text\":\"用户又来问诊\",\"priority\":0},\"children\":[{\"data\":{\"image\":\"\",\"created\":\"1665907210459\",\"id\":\"3ksiqdpmcq2q1g3qa179kn9mpq\",\"imageSize\":{},\"text\":\"飘窗中伴随症状，诊疗经过有内容，其他字段无内容\",\"priority\":0},\"children\":[{\"data\":{\"image\":\"\",\"created\":\"1665907216208\",\"id\":\"3aak14nr0v3op113cngbth32nq\",\"imageSize\":{},\"text\":\"选择就诊人A\",\"priority\":0},\"children\":[{\"data\":{\"image\":\"\",\"created\":\"1665907216197\",\"id\":\"79brrva4s983lr2pd6oc584kj9\",\"imageSize\":{},\"text\":\"再选择就诊人B\",\"priority\":0},\"children\":[{\"data\":{\"image\":\"\",\"created\":\"1665907216196\",\"progress\":9,\"id\":\"47ighrflnq2416p55flb8htgnp\",\"imageSize\":{},\"text\":\"飘窗中内容不变\",\"priority\":2},\"children\":[]},{\"data\":{\"image\":\"\",\"created\":\"1665907216197\",\"progress\":9,\"id\":\"7n9vdija7dhn5dpe9776n3c2a8\",\"imageSize\":{},\"text\":\"就诊人旁边有复诊标\",\"priority\":2},\"children\":[]}]}]}]}]}]}]},\"template\":\"right\",\"theme\":\"classic-compact\",\"version\":\"1.4.43\",\"base\":82,\"right\":1}";
         String caseContent = "{\"root\":{\"data\":{\"created\":1562059643204,\"id\":\"bv8nxhi3c800\",\"text\":\"珠光\"},\"children\":[{\"data\":{\"created\":1669726695791,\"id\":\"coospqmwg2w0\",\"text\":\"ganma\"},\"children\":[{\"data\":{\"created\":1669788218493,\"id\":\"copeitrwlo80\",\"text\":\"分支主题\",\"priority\":1,\"progress\":9},\"children\":[]},{\"data\":{\"created\":1669789395132,\"id\":\"copexubdin40\",\"text\":\"分支主题\",\"priority\":1,\"progress\":9},\"children\":[]},{\"data\":{\"created\":1669790175775,\"id\":\"copf7sxrrjk0\",\"text\":\"fdsafdsa \",\"priority\":1,\"progress\":9},\"children\":[]},{\"data\":{\"created\":1669790286453,\"id\":\"copf97s6k3k0\",\"text\":\"fsdafds \",\"priority\":1,\"progress\":1},\"children\":[]}]},{\"data\":{\"created\":1669726699190,\"id\":\"coosps741lk0\",\"text\":\"toutent \"},\"children\":[{\"data\":{\"created\":1669791150276,\"id\":\"copfk8m8u740\",\"text\":\"我来发送消息\",\"priority\":1,\"progress\":1},\"children\":[]},{\"data\":{\"resource\":[\"预期结果\"],\"created\":1669791358646,\"id\":\"copfmwcb1xk0\",\"text\":\"zhidao\",\"priority\":1,\"progress\":5},\"children\":[]}]},{\"data\":{\"created\":1669726725895,\"id\":\"coosq4grgag0\",\"text\":\"hahf \"},\"children\":[{\"data\":{\"created\":1669798498299,\"id\":\"copi6093y8g0\",\"text\":\"天界\"},\"children\":[{\"data\":{\"created\":1669798599409,\"id\":\"copi7apa0p40\",\"text\":\"懂事了\",\"priority\":2,\"progress\":5},\"children\":[]},{\"data\":{\"created\":1669798605960,\"id\":\"copi7dpmiz40\",\"text\":\"长大了\",\"priority\":2,\"progress\":5},\"children\":[]},{\"data\":{\"created\":1669799178400,\"id\":\"copieooqicw0\",\"text\":\"新增了一条\",\"priority\":2,\"progress\":4},\"children\":[]},{\"data\":{\"created\":1669799190317,\"id\":\"copieu5tls00\",\"text\":\"在增加一条\",\"priority\":2},\"children\":[]}]},{\"data\":{\"created\":1670306257928,\"id\":\"coui5ht11nk0\",\"text\":\"我的新P1用例\",\"priority\":2},\"children\":[]}]},{\"data\":{\"created\":1670404376212,\"id\":\"covgxkq1vow0\",\"text\":\"来来\"},\"children\":[{\"data\":{\"created\":1670404381007,\"id\":\"covgxmxcm3c0\",\"text\":\"新增一条P1\",\"priority\":2},\"children\":[]}]}]},\"template\":\"right\",\"theme\":\"classic-compact\",\"version\":\"1.4.43\",\"base\":92,\"right\":1}";
         JSONObject jsonObject = JSON.parseObject(caseContent);
         RootData rootData = JSON.parseObject(jsonObject.getString("root"),RootData.class);
         List<List<RootData>> lists = parseRootDataToList(rootData);
         //System.out.println("lists = " + lists);
         Map caseMap = new HashMap();
         lists.stream().forEach(listItem -> {
             StringBuilder stringBuilder = new StringBuilder();
            listItem.stream().forEach(item ->
                    stringBuilder.append(item.getData().getText()+"->"));
             caseMap.put(listItem.get(listItem.size()-1).getData().getId(),stringBuilder.toString().substring(0,stringBuilder.toString().length()-2));

         });
          for(Object id : caseMap.keySet()){
              System.out.println(id +": " + caseMap.get(id));
          }



     }
}
