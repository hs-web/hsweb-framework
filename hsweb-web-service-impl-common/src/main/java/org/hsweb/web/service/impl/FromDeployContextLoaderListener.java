package org.hsweb.web.service.impl;

import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.service.form.DynamicFormService;
import org.hsweb.web.service.form.FormService;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class FromDeployContextLoaderListener implements ApplicationListener {

    @Resource
    private FormService formService;
    @Resource
    private DynamicFormService dynamicFormService;

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        QueryParam param = new QueryParam();
        param.where("using", 1);
        try {
            formService.select(param).forEach(form -> {
                try {
                    dynamicFormService.deploy(form);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
