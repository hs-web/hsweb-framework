package org.hswebframework.web.service.form.simple.cluster;

import org.hswebframework.ezorm.rdb.RDBDatabase;
import org.hswebframework.web.entity.form.DynamicFormColumnBindEntity;
import org.hswebframework.web.service.form.DynamicFormService;
import org.hswebframework.web.service.form.FormDeployService;
import org.hswebframework.web.service.form.events.DatabaseInitEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@Component
public class ClusterDatabaseInit {

    @Autowired
    private DynamicFormService dynamicFormService;

    @Autowired
    private FormDeployService formDeployService;

    @EventListener
    public void handleDatabaseInit(DatabaseInitEvent event) {
        RDBDatabase database = event.getDatabase();

        if (database instanceof ClusterDatabase) {
            ClusterDatabase clusterDatabase = ((ClusterDatabase) database);
            clusterDatabase.setVersionChanged(meta -> {
                String formId = meta.getProperty("formId").getValue();
                if (formId != null) {
                    DynamicFormColumnBindEntity entity = dynamicFormService.selectLatestDeployed(formId);
                    if (null != entity) {
                        formDeployService.deploy(entity.getForm(), entity.getColumns(), false);
                    }
                }
            });
            clusterDatabase.setLatestVersionGetter(meta -> {
                String formId = meta.getProperty("formId").getValue();
                if (formId != null) {
                    return dynamicFormService.selectDeployedVersion(formId);
                }
                return -1L;
            });
        }
    }
}
