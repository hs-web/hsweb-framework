package org.hsweb.web.controller.module;

import org.hsweb.web.bean.po.module.ModuleMeta;
import org.hsweb.web.bean.po.role.UserRole;
import org.hsweb.web.bean.po.user.User;
import org.hsweb.web.controller.GenericController;
import org.hsweb.web.core.authorize.annotation.Authorize;
import org.hsweb.web.core.message.ResponseMessage;
import org.hsweb.web.core.utils.WebUtil;
import org.hsweb.web.service.module.ModuleMetaService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Created by zhouhao on 16-5-10.
 */
@RestController
@RequestMapping("/module-meta")
@Authorize(module = "module-meta")
public class ModuleMetaController extends GenericController<ModuleMeta, String> {
    @Resource
    private ModuleMetaService moduleMetaService;

    @Override
    protected ModuleMetaService getService() {
        return moduleMetaService;
    }

    @RequestMapping(value = "/{key}/own", method = RequestMethod.GET)
    public ResponseMessage userModuleMeta(@PathVariable String key) throws Exception {
        User user = WebUtil.getLoginUser();
        List<UserRole> roles = user.getUserRoles();
        String[] roleIdList = roles
                .stream()
                .map(userRole -> userRole.getRoleId())
                .collect(Collectors.toList()).toArray(new String[roles.size()]);
        return ResponseMessage.ok(getService().selectByKeyAndRoleId(key, roleIdList));
    }
}
