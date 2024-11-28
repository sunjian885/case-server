package com.xiaoju.framework.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.xiaoju.framework.constants.SystemConstant;
import com.xiaoju.framework.constants.enums.StatusCode;
import com.xiaoju.framework.entity.dto.Authority;
import com.xiaoju.framework.entity.dto.User;
import com.xiaoju.framework.entity.dto.UserBaseInfo;
import com.xiaoju.framework.entity.exception.CaseServerException;
import com.xiaoju.framework.entity.request.auth.UserLoginReq;
import com.xiaoju.framework.entity.request.auth.UserRegisterReq;
import com.xiaoju.framework.mapper.AuthorityMapper;
import com.xiaoju.framework.mapper.CaseBackupMapper;
import com.xiaoju.framework.mapper.TestCaseMapper;
import com.xiaoju.framework.mapper.UserMapper;
import com.xiaoju.framework.service.UserService;
import com.xiaoju.framework.util.CodecUtils;
import com.xiaoju.framework.util.CookieUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by didi on 2021/4/22.
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    /**
     * 保存对应权限的路径匹配列表
     * <br/>PS:用户登陆刷新对应角色权限
     */
    private final Map<String, List<String>> roleAuthority = new HashMap<>();

    @Value("${authority.flag}")
    private Boolean authorityFlag;

    @Value("${sa-token.token-name}")
    private String tokenName;
    @Resource
    private UserMapper userMapper;

    @Resource
    private TestCaseMapper testCaseMapper;

    @Resource
    private CaseBackupMapper caseBackupMapper;


    @Resource
    private AuthorityMapper authorityMapper;

    private static UserBaseInfo transferToUserBaseInfo(User user) {
        //LOGGER.info("user real name is {}", user.getRealName());
        UserBaseInfo userBaseInfo = new UserBaseInfo();
        userBaseInfo.setUserid(user.getUserid());
        userBaseInfo.setId(user.getId());
        userBaseInfo.setUsername(user.getUsername());
        userBaseInfo.setRealName(user.getRealName());
        return userBaseInfo;
    }

    @Override
    public User register(UserRegisterReq req, HttpServletRequest request, HttpServletResponse response) {
        //log.info("req={},request={},response={}",req,request,response);
        //1.检查数据库中是否已经存在该用户
        User dbuser = userMapper.selectByUserName(req.getUsername());
        if (dbuser != null) {
            throw new CaseServerException("用户名已存在", StatusCode.INTERNAL_ERROR);
        }

        User user = new User();
        LOGGER.info("req.getPassword() =={}",req.getPassword());
        //2.生成盐，对密码进行加密再保存到数据库中
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);
        user.setPassword(CodecUtils.md5Hex(req.getPassword(),salt));
        user.setUserid(req.getUserid());
        user.setRealName(req.getRealName());
        user.setUsername(req.getUsername());
        user.setChannel(1);
        user.setProductLineId(1L);
        user.setIsDelete(0);

        //3.将新用户数据保存到数据库中
        userMapper.insertSelective(user);
        User myUser = userMapper.selectByUserName(req.getUsername());
        StpUtil.login(myUser.getUserid());
        //4.将新用户设置到cookie中去
        CookieUtils.setCookie(request, response, "username", req.getRealName(), 60 * 60 * 24, null, false);
        CookieUtils.setCookie(request, response, tokenName, StpUtil.getTokenValue(), 60 * 60 * 24 * 7, null, false);

        return userMapper.selectByUserName(req.getUsername());

    }

    @Override
    public User login(UserLoginReq req, HttpServletRequest request, HttpServletResponse response) {
        log.info("user login params req={},request={},response={}",req,request,response);
        //1.检查数据库中是否存在该用户
        User dbuser = userMapper.selectByUserName(req.getUsername());
        if (dbuser == null) {
            throw new CaseServerException("用户名不存在", StatusCode.INTERNAL_ERROR);
        }

        //2.校验密码是否正确
        if (!dbuser.getPassword().equals(CodecUtils.md5Hex(req.getPassword(),dbuser.getSalt()))) {
            throw new CaseServerException("密码错误",StatusCode.INTERNAL_ERROR);
        }

        //sa-token使用userid登录用户
        StpUtil.login(dbuser.getUserid());
        //3.将新用户设置到cookie中去
        CookieUtils.setCookie(request, response, "username", dbuser.getRealName(), 60 * 60 * 24 * 7, null, false);
        CookieUtils.setCookie(request, response, tokenName, StpUtil.getTokenValue(), 60 * 60 * 24 * 7, null, false);
        //4.开启权限时，主动刷新对应权限信息缓存
        if (authorityFlag) {
            String authorityName = dbuser.getAuthorityName();
            if (StringUtils.isEmpty(authorityName)) {
                authorityName = SystemConstant.DEFAULT_AUTHORITY_NAME;
            }
            Authority authority = authorityMapper.selectByAuthorityName(authorityName);
            if (Objects.nonNull(authority)) {
                String[] authorityContentArray = authority.getAuthorityContent().split(SystemConstant.COMMA);
                roleAuthority.put(authority.getAuthorityName(), Arrays.asList(authorityContentArray));
                LOGGER.info("刷新权限信息，authorityName: {}", authorityName);
            }
        }
        log.info("user login response dbuser={},request={},response={}",dbuser,request,response);
        return dbuser;
    }

    @Override
    public Integer logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {

            //删除cookie中的username
            if (cookie.getName().equals("username")) {
                Cookie newcookie = new Cookie(cookie.getName(),null);

                //路径path相同才会被判定为同名cookie，才能达到覆盖效果
                newcookie.setPath("/");
                newcookie.setMaxAge(0);

                response.addCookie(newcookie);
            }

            //删除cookie中的jsessionid
            if (cookie.getName().equals("JSESSIONID")) {
                Cookie newcookie = new Cookie(cookie.getName(),null);

                //路径path相同才会被判定为同名cookie，才能达到覆盖效果
                newcookie.setPath("/");
                newcookie.setMaxAge(0);

                response.addCookie(newcookie);
            }
        }
        StpUtil.logout(StpUtil.getLoginId());
        return null;
    }

    @Override
    public User registerOrLogin(UserRegisterReq req, HttpServletRequest request, HttpServletResponse response) {
        User user;
        //1.检查数据库中是否存在该用户
        //LOGGER.info("用户名，username: {}", req.getUsername());
        User dbuser = userMapper.selectByUserName(req.getUsername());
        //LOGGER.info("用户名，dbuser: {}", dbuser);
        if (dbuser == null) {
            user = this.register(req,request,response);

        } else {
            user = dbuser;
            //2.校验密码是否正确
            if (!dbuser.getPassword().equals(CodecUtils.md5Hex(req.getPassword(),dbuser.getSalt()))) {
                throw new CaseServerException("密码错误",StatusCode.INTERNAL_ERROR);
            }
        }
        //sa-token使用userid登录用户
        StpUtil.login(user.getUserid());
        //3.将新用户设置到cookie中去
        CookieUtils.setCookie(request, response, "username", req.getRealName(), 60 * 60 * 24 * 7, null, false);
        CookieUtils.setCookie(request, response, tokenName, StpUtil.getTokenValue(), 60 * 60 * 24 * 7, null, false);
        return user;
    }

    @Override
    public List<String> getUserAuthorityContent(String username) {
        User user = userMapper.selectByUserName(username);
        if (Objects.isNull(user)) {
            LOGGER.info("用户名不存在，username: {}", username);
            throw new SecurityException("认证失败");
        }
        String authorityName = user.getAuthorityName();
        if (StringUtils.isEmpty(authorityName)) {
            authorityName = SystemConstant.DEFAULT_AUTHORITY_NAME;
        }
        List<String> authorityContent = roleAuthority.get(authorityName);
        // 缓存中不存在，则查询数据库，添加到缓存中
        if (Objects.isNull(authorityContent)) {
            // 通过权限名称查询权限
            Authority authority = authorityMapper.selectByAuthorityName(authorityName);
            String[] authorityContentArray = authority.getAuthorityContent().split(SystemConstant.COMMA);
            authorityContent = Arrays.asList(authorityContentArray);
            // 添加权限名称到缓存中
            roleAuthority.put(authority.getAuthorityName(), Arrays.asList(authorityContentArray));
        }
        // 数据库搜不到，则直接使用默认权限
        if (CollectionUtils.isEmpty(authorityContent)) {
            authorityContent = roleAuthority.get(SystemConstant.DEFAULT_AUTHORITY_NAME);
        }
        return authorityContent;
    }

    @Override
    public List<UserBaseInfo> getAllUser() {
        return userMapper.getAllUser().stream().map(UserServiceImpl::transferToUserBaseInfo).collect(Collectors.toList());
    }

    @Override
    public void fixUserid() {
        //查询到所有的用户信息
        Map<String, Long> usernames = userMapper.getAllUser().stream().collect(Collectors.toMap(User::getRealName, User::getUserid));
        //然后找到对应的用例
        usernames.keySet().stream().forEach(name ->{
            long userid = usernames.get(name);
            //TODO 更新用户名
            testCaseMapper.updateTestCaseCreatorId(userid,name);
            testCaseMapper.updateTestCaseModifierId(userid,name);
            caseBackupMapper.updateBackUpUserid(userid,name);
        });
    }
}
