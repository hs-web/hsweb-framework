package org.hsweb.web.service.impl;

import org.hsweb.web.service.form.FormService;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class FormDeployContextLoaderListener implements ApplicationListener<ContextRefreshedEvent> {
    @Resource
    private FormService        formService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() != null) return;
        formService.tryDeployAll();
    }
}
