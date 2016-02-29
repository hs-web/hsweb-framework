package org.hsweb.web.controller.user;

import org.hsweb.web.logger.AccessLogger;
import org.hsweb.web.authorize.Authorize;
import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.bean.po.user.User;
import org.hsweb.web.controller.GenericController;
import org.hsweb.web.message.ResponseMessage;
import org.hsweb.web.service.user.UserService;
import org.springframework.web.bind.annotation.*;

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
    public ResponseMessage list(QueryParam param) {
        param.excludes("password");
        return super.list(param)
                .exclude(User.class, "password", "modules", "userRoles")
                .onlyData();
    }

    @Override
    @AccessLogger("获取用户详情")
    public ResponseMessage info(@PathVariable("id") String id) {
        return super.info(id).exclude(User.class, "password", "modules");
    }

    @Override
    @AccessLogger("删除")
    public ResponseMessage delete(@PathVariable("id") String id) {
        try {
            User user = getService().selectByPk(id);
            if (user == null) return new ResponseMessage(false, "该用户不存在!", "404");
            user.setStatus(-1);
            getService().update(user);
            return new ResponseMessage(true, "删除成功");
        } catch (Exception e) {
            return new ResponseMessage(false, e);
        }
    }


}
