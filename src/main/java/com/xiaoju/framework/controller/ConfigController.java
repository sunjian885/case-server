package com.xiaoju.framework.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.xiaoju.framework.entity.dto.Config;
import com.xiaoju.framework.entity.response.controller.Response;
import com.xiaoju.framework.mapper.ConfigMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping(value = "/api/config")
public class ConfigController {

    @Resource
    ConfigMapper configMapper;

    @GetMapping(value = "/get")
    @SaCheckLogin
    public Response<Config> getConfig(@RequestParam @NotNull(message = "类型为空") String type,
                            @RequestParam @NotNull(message = "Key为空") String key){
        return Response.success(configMapper.getConfig(type, key));
    }

    @GetMapping(value = "/getAll")
    @SaCheckLogin
    public Response<List<Config>> getAllConfig(@RequestParam @NotNull(message = "类型为空") String type){
        return Response.success(configMapper.getAllByType(type));
    }
}
