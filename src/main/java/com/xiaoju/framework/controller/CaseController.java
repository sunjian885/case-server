package com.xiaoju.framework.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xiaoju.framework.constants.enums.StatusCode;
import com.xiaoju.framework.entity.exception.CaseServerException;
import com.xiaoju.framework.entity.persistent.CaseBackup;
import com.xiaoju.framework.entity.persistent.TestCase;
import com.xiaoju.framework.entity.request.cases.*;
import com.xiaoju.framework.entity.request.ws.WsSaveReq;
import com.xiaoju.framework.entity.response.controller.Response;
import com.xiaoju.framework.service.AuthorityService;
import com.xiaoju.framework.service.CaseBackupService;
import com.xiaoju.framework.service.CaseService;
import com.xiaoju.framework.util.TreeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 用例相关接口
 *
 * @author didi
 * @date 2020/11/20
 */
@RestController
@CrossOrigin
@RequestMapping("/api/case")
public class CaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CaseController.class);

    @Resource
    CaseService caseService;

    @Resource
    CaseBackupService caseBackupService;

    @Resource
    AuthorityService authorityService;
    /**
     * 用例 - 根据文件夹id获取所有用例
     *
     * @param productLineId 业务线id
     * @param bizId 用例id
     * @param title 用例标题
     * @param creator 创建人前缀
     * @param requirementId 需求id
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @param channel 1
     * @param pageNum 页码
     * @param pageSize 页面承载量
     * @return 分页接口
     */
    @GetMapping(value = "/list")
    @SaCheckLogin
    public Response<?> getCaseList(@RequestParam @NotNull(message = "渠道为空")  Integer channel,
                                   @RequestParam @NotNull(message = "业务线id为空")  Long productLineId,
                                   @RequestParam @NotNull(message = "文件夹未选中")  String bizId,
                                   @RequestParam(required = false)  Long userid,
                                   @RequestParam(required = false)  String caseRequestType,
                                   @RequestParam(required = false)  String title,
                                   @RequestParam(required = false)  String creator,
                                   @RequestParam(required = false)  String requirementId,
                                   @RequestParam(required = false)  String caseKeyWords,
                                   @RequestParam(required = false)  String beginTime,
                                   @RequestParam(required = false)  String endTime,
                                   @RequestParam(defaultValue = "1") Integer pageNum,
                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        //LOGGER.info("caseRequestType = {}",caseRequestType);
        return Response.success(caseService.getCaseList(
                new CaseQueryReq(0, title, creator, requirementId, beginTime,
                        endTime, channel, bizId, productLineId, caseKeyWords, pageNum, pageSize,userid,caseRequestType)));
    }

    /**
     * 列表 - 创建或者复制用例
     *
     * @param request 请求体
     * @return 响应体
     */
    @PostMapping(value = "/create")
    public Response<?> createOrCopyCase(@RequestBody CaseCreateReq request) {
        request.validate();
        try {
            return Response.success(caseService.insertOrDuplicateCase(request));
        } catch (CaseServerException e) {
            throw new CaseServerException(e.getLocalizedMessage(), e.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("[Case Create]Create or duplicate test case failed. params={}, e={} ", request.toString(), e.getMessage());
            return Response.build(StatusCode.SERVER_BUSY_ERROR);
        }
    }

    /**
     * 列表 - 修改用例属性
     *
     * @param request 请求体
     * @return 响应体
     */
    @PostMapping(value = "/edit")
    public Response<?> editCase(@RequestBody CaseEditReq request) {
        request.validate();
        try {
            return Response.success(caseService.updateCase(request));
        } catch (CaseServerException e) {
            throw new CaseServerException(e.getLocalizedMessage(), e.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("[Case Update]Update test case failed. params={} e={} ", request.toString(), e.getMessage());
            return Response.build(StatusCode.SERVER_BUSY_ERROR);
        }
    }

    /**
     * 列表 - 删除用例
     *
     * @param request 请求体
     * @return 响应体
     */
    @PostMapping(value = "/delete")
    @SaCheckLogin
    public Response<?> deleteCase(@RequestBody CaseDeleteReq request) {
        request.validate();
        //TODO: 判断是否有权限删除当前用例，必须是本人或者是管理员才可以删除
        if(!authorityService.canDeleteCaseById(request.getId())){
            return Response.build(StatusCode.AUTHORITY_LIMIT.getStatus(),StatusCode.AUTHORITY_LIMIT.getMsg());
        }
        try {
            return Response.success(caseService.deleteCase(request.getId()));
        } catch (CaseServerException e) {
            throw new CaseServerException(e.getLocalizedMessage(), e.getStatus());
        } catch (Exception e) {
            LOGGER.error("[Case Delete]Delete test case failed. params={} e={} ", request.toString(), e.getMessage());
            e.printStackTrace();
            return Response.build(StatusCode.SERVER_BUSY_ERROR);
        }
    }

    /**
     * 列表 - 查看用例详情
     *
     * @param caseId 用例id
     * @return 响应体
     */
    @GetMapping(value = "/detail")
    @SaCheckLogin
    public Response<?> getCaseDetail(@RequestParam @NotNull(message = "用例id为空") Long caseId) {
        try {
            return Response.success(caseService.getCaseDetail(caseId));
        } catch (CaseServerException e) {
            throw new CaseServerException(e.getLocalizedMessage(), e.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("[Case detail]View detail of test case failed. params={}, e={} ", caseId, e.getMessage());
            return Response.build(StatusCode.SERVER_BUSY_ERROR);
        }
    }

    /**
     * 配合list 筛选时获取所有创建人的列表
     *
     * @param caseType 用例类型
     * @param productLineId 业务线id
     * @return 响应体
     */
    @GetMapping(value = "/listCreators")
    @SaCheckLogin
    public Response<?> listCreators(@RequestParam @NotNull(message = "用例类型为空") Integer caseType,
                                    @RequestParam @NotNull(message = "业务线为空") Long productLineId) {
        return Response.success(caseService.listCreators(caseType, productLineId));
    }

    /**
     * 配合detail 修改圈选用例时统计的用例条目数据
     *
     * @param caseId 用例id
     * @param priority 优先级列表
     * @param resource 资源列表
     * @return 响应体
     */
    @GetMapping(value = "/countByCondition")
    @SaCheckLogin
    public Response<?> getCountByCondition(@RequestParam @NotNull(message = "用例id为空") Long caseId,
                                           @RequestParam @NotNull(message = "圈选优先级为空") String[] priority,
                                           @RequestParam @NotNull(message = "圈选资源为空") String[] resource) {
        CaseConditionReq req = new CaseConditionReq(caseId, priority, resource);
        req.validate();
        return Response.success(caseService.getCountByCondition(req));
    }

    /**
     * 脑图 - 获取上方用例概览信息
     *
     * @param id 用例id
     * @return 概览信息
     */
    @GetMapping(value = "/getCaseInfo")
    @SaCheckLogin
    public Response<?> getCaseGeneralInfo(@RequestParam @NotNull(message = "用例id为空") Long id) {
        return Response.success(caseService.getCaseGeneralInfo(id));
    }

    /**
     * 脑图 - 保存按钮 可能是case也可能是record
     *
     * @param req 请求体
     * @return 响应体
     */
    @PostMapping(value = "/update")
    @SaCheckLogin
    public Response<?> updateWsCase(@RequestBody WsSaveReq req) {
        //LOGGER.info("case update exec");
        try {
            caseService.wsSave(req);
            return Response.success();
        } catch (CaseServerException e) {
            throw new CaseServerException(e.getLocalizedMessage(), e.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
            //LOGGER.error("[Case Update]Update test case failed. params={} e={} ", req.toString(), e.getMessage());
            return Response.build(StatusCode.SERVER_BUSY_ERROR);
        }
    }

    @PostMapping(value = "/rollbackCase")
    @SaCheckLogin
    public Response<?> rollbackCaseByBackupId(@RequestBody String content){
        LOGGER.info("rollbackCaseByBackupId content {}",content);
        JSONObject jsonObject = JSON.parseObject(content);
         Long userId = StpUtil.getLoginIdAsLong();
         StpUtil.getSession();
        Long id = Long.valueOf(jsonObject.getString("id"));
        //通过id获取caseId
        CaseBackup backupById = caseBackupService.getBackupById(id);
        ////取出当前的用例内容，然后放入backUp用例里面
        TestCase testCase = caseService.getCaseInfoByCaseId(backupById.getCaseId());
        CaseBackup caseBackup = new CaseBackup();
        caseBackup.setCaseId(testCase.getId());
        caseBackup.setCaseContent(testCase.getCaseContent());
        caseBackup.setTitle(testCase.getTitle());
        caseBackup.setCreator(testCase.getCreator());
        caseBackup.setUserId(userId);
        caseBackup.setIsDelete(0);
        caseBackupService.insertBackup(caseBackup);
        ////将当前的backUp用例放入当前用例的数据库中，其实缓存中也应该要处理下，但是目前都是放在线程缓存中，没法获取

        testCase.setGmtModified(new Date());
        testCase.setCaseContent(backupById.getCaseContent());
        //testCase.setModifier();
        testCase.setModifierId(userId);
        Integer leafNodeCount = TreeUtil.getLeafNodeCount(backupById.getCaseContent());
        testCase.setAmount(leafNodeCount);
        LOGGER.info("rollbackCaseByBackupId update case info == {}",testCase);
        caseService.updateCaseContent(testCase);
        return Response.success();
    }


    @PostMapping(value = "/fixAmount")
    public Response<?> updateWsCase( ) {
        caseService.fixAmount();
        return Response.success();
    }
}
