package org.hsweb.web.controller.login;

import org.hsweb.web.core.authorize.annotation.Authorize;
import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.bean.po.module.Module;
import org.hsweb.web.bean.po.user.User;
import org.hsweb.web.core.message.ResponseMessage;
import org.hsweb.web.service.module.ModuleService;
import org.hsweb.web.core.utils.WebUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.webbuilder.utils.common.StringUtils;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by zhouhao on 16-4-13.
 */
@RestController
@RequestMapping("/userModule")
@Authorize
public class UserModuleController {
    @Resource
    public ModuleService moduleService;

    @RequestMapping
    public ResponseMessage userModule() throws Exception {
        String[] includes = {
                "name", "u_id", "p_id", "icon", "uri", "m_option"
        };
        User user = WebUtil.getLoginUser();
        List<Module> modules;
        if (user == null) {
            modules = moduleService.select(new QueryParam().includes(includes).orderBy("sort_index"));
            modules = modules.stream()
                    .filter(module -> {
                        Object obj = module.getM_optionMap().get("M");
                        if (obj instanceof Map)
                            return StringUtils.isTrue(((Map) obj).get("checked"));
                        return false;
                    })
                    .collect(Collectors.toCollection(() -> new LinkedList<>()));
        } else {
            modules = user.getModules().stream()
                    .filter(module -> user.hasAccessModuleAction(module.getU_id(), "M"))
                    .collect(Collectors.toCollection(() -> new LinkedList<>()));
        }

        return ResponseMessage.ok(modules)
                .include(Module.class, includes)
                .exclude(Module.class, "m_option")
                .onlyData();
    }
}
