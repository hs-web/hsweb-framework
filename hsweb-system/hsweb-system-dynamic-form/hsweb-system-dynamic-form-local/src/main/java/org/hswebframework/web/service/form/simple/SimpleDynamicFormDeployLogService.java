package org.hswebframework.web.service.form.simple;

import org.hswebframework.ezorm.rdb.operator.dml.query.SortOrder;
import org.hswebframework.web.commons.entity.DataStatus;
import org.hswebframework.web.entity.form.DynamicFormDeployLogEntity;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.service.form.DynamicFormDeployLogService;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("dynamicFormDeployLogService")
public class SimpleDynamicFormDeployLogService extends GenericEntityService<DynamicFormDeployLogEntity, String>
        implements DynamicFormDeployLogService {

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public DynamicFormDeployLogEntity selectLastDeployed(String formId) {
        Objects.requireNonNull(formId);
        DynamicFormDeployLogEntity deployed = createQuery()
                .where(DynamicFormDeployLogEntity.formId, formId)
                .orderBy(SortOrder.desc(DynamicFormDeployLogEntity.deployTime))
                .fetchOne().orElse(null);
        if (null != deployed && DataStatus.STATUS_ENABLED.equals(deployed.getStatus())) {
            return deployed;
        }
        return null;
    }

    @Override
    public DynamicFormDeployLogEntity selectDeployed(String formId, long version) {
        Objects.requireNonNull(formId);
        DynamicFormDeployLogEntity deployed = createQuery()
                .where(DynamicFormDeployLogEntity.formId, formId)
                .and(DynamicFormDeployLogEntity.version, version)
                .orderBy(SortOrder.desc(DynamicFormDeployLogEntity.deployTime))
                .fetchOne().orElse(null);
        if (null != deployed && DataStatus.STATUS_ENABLED.equals(deployed.getStatus())) {
            return deployed;
        }
        return null;
    }

    @Override
    public void cancelDeployed(String formId) {
        Objects.requireNonNull(formId);
        DynamicFormDeployLogEntity deployed = createQuery()
                .where(DynamicFormDeployLogEntity.formId, formId)
                .orderBy(SortOrder.desc(DynamicFormDeployLogEntity.deployTime))
                .fetchOne().orElse(null);
        if (deployed != null) {
            createUpdate()
                    .set(DynamicFormDeployLogEntity.status, DataStatus.STATUS_DISABLED)
                    .where(DynamicFormDeployLogEntity.id, deployed.getId())
                    .execute();
        }
    }

    @Override
    public void cancelDeployed(String formId, long version) {
        Objects.requireNonNull(formId);
        createUpdate()
                .set(DynamicFormDeployLogEntity.status, DataStatus.STATUS_DISABLED)
                .where(DynamicFormDeployLogEntity.formId, formId)
                .and(DynamicFormDeployLogEntity.version, version)
                .execute();
    }
}
