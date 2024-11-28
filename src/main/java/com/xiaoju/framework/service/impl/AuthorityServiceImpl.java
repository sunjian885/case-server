package com.xiaoju.framework.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.xiaoju.framework.entity.persistent.TestCase;
import com.xiaoju.framework.service.AuthorityService;
import com.xiaoju.framework.service.CaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class AuthorityServiceImpl implements AuthorityService {


    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorityServiceImpl.class);

    @Resource
    public CaseService caseService;


    /**
     * 只有用例集创建者本人可以操作删除
     * @param caseId
     * @return
     */
    @Override
    public Boolean canDeleteCaseById(Long caseId) {
        long userid = StpUtil.getLoginIdAsLong();
        //LOGGER.info("canDeleteCase userid ={}",userid);
        TestCase testCase = caseService.getCaseInfoByCaseId(caseId);
        LOGGER.info("canDeleteCase testCase ={}",testCase);
        return testCase.getCreatorId() == userid;
    }
}
