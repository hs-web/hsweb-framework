package org.hsweb.web.controller.template;

import org.hsweb.web.bean.po.template.Template;
import org.hsweb.web.controller.GenericController;
import org.hsweb.web.service.template.TemplateService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * Created by zhouhao on 16-5-23.
 */
@RestController
@RequestMapping("/template")
public class TemplateController extends GenericController<Template, String> {
    @Resource
    private TemplateService templateService;

    @Override
    protected TemplateService getService() {
        return templateService;
    }

}
