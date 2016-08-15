package org.hsweb.web.service.impl;

import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.service.form.DynamicFormService;
import org.hsweb.web.service.form.FormService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class FormDeployContextLoaderListener implements ApplicationListener<ContextRefreshedEvent> {
    @Resource
    private FormService formService;
    @Resource
    private DynamicFormService dynamicFormService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() != null) return;
        QueryParam param = new QueryParam();
        param.where("using", 1).noPaging();
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
