package org.hsweb.web.controller.user;

import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.bean.po.user.User;
import org.hsweb.web.controller.GenericController;
import org.hsweb.web.core.authorize.annotation.Authorize;
import org.hsweb.web.core.logger.annotation.AccessLogger;
import org.hsweb.web.core.message.ResponseMessage;
import org.hsweb.web.service.user.UserService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 后台管理用户控制器，继承自GenericController,使用rest+json
 * Created by generator 2015-8-26 10:35:57
 */
@RestController
@RequestMapping(value = "/user")
@AccessLogger("用户管理")
@Authorize(module = "user")
public class UserController extends GenericController<User, String> {

    //默认服务类
    @Resource
    private UserService userService;

    @Override
    public UserService getService() {
        return this.userService;
    }

    @Override
    @AccessLogger("获取列表")
    public ResponseMessage list(QueryParam param)  {
        param.excludes("password");
        return super.list(param)
                .exclude(User.class, "password", "modules", "userRoles")
                .onlyData();
    }

    @Override
    @AccessLogger("获取用户详情")
    public ResponseMessage info(@PathVariable("id") String id)  {
        return super.info(id).exclude(User.class, "password", "modules");
    }

    @AccessLogger("禁用")
    @RequestMapping(value = "/{id}/disable", method = RequestMethod.PUT)
    @Authorize(action = "disable")
    public ResponseMessage disable(@PathVariable("id") String id)  {
        getService().disableUser(id);
        return ResponseMessage.ok();
    }

    @AccessLogger("启用")
    @Authorize(action = "enable")
    @RequestMapping(value = "/{id}/enable", method = RequestMethod.PUT)
    public ResponseMessage enable(@PathVariable("id") String id)  {
        getService().enableUser(id);
        return ResponseMessage.ok();
    }

}
