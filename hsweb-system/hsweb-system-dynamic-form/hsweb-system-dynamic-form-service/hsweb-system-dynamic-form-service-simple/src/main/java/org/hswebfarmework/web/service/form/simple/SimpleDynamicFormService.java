package org.hswebfarmework.web.service.form.simple;

import org.hsweb.ezorm.core.Insert;
import org.hsweb.ezorm.core.dsl.Delete;
import org.hsweb.ezorm.core.dsl.Query;
import org.hsweb.ezorm.core.dsl.Update;
import org.hswebfarmework.web.dao.form.DynamicFormDao;
import org.hswebfarmework.web.entity.form.DynamicFormEntity;
import org.hswebframework.web.commons.entity.param.DeleteParamEntity;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.commons.entity.param.UpdateParamEntity;
import org.hswebframework.web.concurrent.lock.annotation.WriteLock;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.id.IDGenerator;
import org.hswebfarmework.web.service.form.DynamicFormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("dynamicFormService")
public class SimpleDynamicFormService extends GenericEntityService<DynamicFormEntity, String>
        implements DynamicFormService {
    @Autowired
    private DynamicFormDao dynamicFormDao;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public DynamicFormDao getDao() {
        return dynamicFormDao;
    }

    @Override
    public int updateByPk(String s, DynamicFormEntity entity) {
        return super.updateByPk(s, entity);
    }

    public void deploy(String formId) {

    }

    @Override
    public void unDeploy(String formId) {

    }

    @Override
    public <T> Query<T, QueryParamEntity> createQuery(String formName) {
        return null;
    }

    @Override
    public <T> Update<T, UpdateParamEntity<T>> createUpdate(String formName) {
        return null;
    }

    @Override
    public <T> Insert<T> createInsert(String formName) {
        return null;
    }

    @Override
    public Delete<DeleteParamEntity> createDelete(String formName) {
        return null;
    }
}
