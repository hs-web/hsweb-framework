package org.hsweb.web.service.impl;

import org.hsweb.ezorm.rdb.meta.RDBTableMetaData;
import org.hsweb.web.bean.po.form.Form;
import org.hsweb.web.service.form.DynamicFormService;
import org.hsweb.web.service.form.FormService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class FormDeployContextLoaderListener implements ApplicationListener<ContextRefreshedEvent> {
    @Resource
    private FormService        formService;
    @Resource
    private DynamicFormService dynamicFormService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() != null) return;
        try {
            formService.createQuery().where(Form.Property.using, 1).listNoPaging().forEach(form -> {
                try {
                    Form deployed = formService.selectDeployed(form.getName());
                    if (null != deployed) {
                        RDBTableMetaData metaData = dynamicFormService.parseMeta(deployed);
                        dynamicFormService.getDefaultDatabase().reloadTable(metaData);
                    } else {
                        dynamicFormService.deploy(form);
                    }
                } catch (Exception e) {
                    logger.error("部署{}:({})失败", form.getName(), form.getRemark(), e);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
