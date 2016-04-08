package org.hsweb.web.controller.form;

import org.hsweb.web.logger.annotation.AccessLogger;
import org.hsweb.web.authorize.annotation.Authorize;
import org.hsweb.web.bean.po.form.Form;
import org.hsweb.web.controller.GenericController;
import org.hsweb.web.service.form.FormService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 自定义表单控制器，继承自GenericController,使用rest+json
 * Created by generator(by 周浩) 2015-8-1 16:31:30
 */
@RestController
@RequestMapping(value = "/form")
@AccessLogger("表单管理")
@Authorize(role = "form")
public class FormController extends GenericController<Form, String> {

    //默认服务类
    @Resource
    private FormService formService;

    @Override
    public FormService getService() {
        return this.formService;
    }

}
