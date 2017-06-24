package org.hswebfarmework.web.service.form;

import org.hsweb.ezorm.core.Insert;
import org.hsweb.ezorm.core.dsl.Delete;
import org.hsweb.ezorm.core.dsl.Query;
import org.hsweb.ezorm.core.dsl.Update;
import org.hsweb.ezorm.core.param.InsertParam;
import org.hswebfarmework.web.entity.form.DynamicFormEntity;
import org.hswebframework.web.commons.entity.param.DeleteParamEntity;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.commons.entity.param.UpdateParamEntity;
import org.hswebframework.web.service.CrudService;

/**
 * 动态表单 服务类
 *
 * @author hsweb-generator-online
 */
public interface DynamicFormService extends CrudService<DynamicFormEntity, String> {

    void deploy(String formId);

    void unDeploy(String formId);

    <T> Query<T, QueryParamEntity> createQuery(String formName);

    <T> Update<T, UpdateParamEntity<T>> createUpdate(String formName);

    <T> Insert<T> createInsert(String formName);

    Delete<DeleteParamEntity> createDelete(String formName);


}
