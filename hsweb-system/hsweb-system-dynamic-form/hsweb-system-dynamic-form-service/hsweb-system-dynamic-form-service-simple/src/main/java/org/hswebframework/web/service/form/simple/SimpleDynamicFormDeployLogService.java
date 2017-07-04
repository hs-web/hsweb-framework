package org.hswebframework.web.service.form.simple;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.hswebframework.web.commons.entity.DataStatus;
import org.hswebframework.web.dao.form.DynamicFormDeployLogDao;
import org.hswebframework.web.entity.form.DynamicFormColumnEntity;
import org.hswebframework.web.entity.form.DynamicFormDeployLogEntity;
import org.hswebframework.web.entity.form.DynamicFormEntity;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.form.DynamicFormDeployLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("dynamicFormDeployLogService")
public class SimpleDynamicFormDeployLogService extends GenericEntityService<DynamicFormDeployLogEntity, String>
        implements DynamicFormDeployLogService {
    @Autowired
    private DynamicFormDeployLogDao dynamicFormDeployLogDao;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public DynamicFormDeployLogDao getDao() {
        return dynamicFormDeployLogDao;
    }

    @Override
    public DynamicFormDeployLogEntity selectLastDeployed(String formId) {
        Objects.requireNonNull(formId);
        DynamicFormDeployLogEntity deployed = createQuery()
                .where(DynamicFormDeployLogEntity.formId, formId)
                .orderByDesc(DynamicFormDeployLogEntity.deployTime)
                .single();
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
                .orderByDesc(DynamicFormDeployLogEntity.deployTime)
                .single();
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
                .orderByDesc(DynamicFormDeployLogEntity.deployTime)
                .single();
        if (deployed != null) {
            createUpdate()
                    .set(DynamicFormDeployLogEntity.status, DataStatus.STATUS_DISABLED)
                    .where(DynamicFormDeployLogEntity.id, deployed.getId())
                    .exec();
        }
    }

    @Override
    public void cancelDeployed(String formId, long version) {
        Objects.requireNonNull(formId);
        createUpdate()
                .set(DynamicFormDeployLogEntity.status, DataStatus.STATUS_DISABLED)
                .where(DynamicFormDeployLogEntity.formId, formId)
                .and(DynamicFormDeployLogEntity.version, version)
                .exec();
    }
}
