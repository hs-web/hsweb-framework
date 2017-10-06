package org.hswebframework.web.service.form;

import org.hswebframework.web.entity.form.DynamicFormColumnBindEntity;
import org.hswebframework.web.entity.form.DynamicFormColumnEntity;
import org.hswebframework.web.entity.form.DynamicFormEntity;
import org.hswebframework.web.service.CrudService;

import java.util.List;

/**
 * 动态表单 服务类
 *
 * @author hsweb-generator-online
 */
public interface DynamicFormService extends CrudService<DynamicFormEntity, String> {
    void deployAllFromLog();

    void deployAll();

    void deploy(String formId);

    void unDeploy(String formId);

    String saveOrUpdate(DynamicFormColumnBindEntity bindEntity);

    String saveOrUpdateColumn(DynamicFormColumnEntity columnEntity);

    List<String> saveOrUpdateColumn(List<DynamicFormColumnEntity> columnEntities);

    DynamicFormColumnEntity deleteColumn(String id);

    List<DynamicFormColumnEntity> deleteColumn(List<String> ids);

    List<DynamicFormColumnEntity> selectColumnsByFormId(String formId);

    DynamicFormColumnBindEntity selectLatestDeployed(String formId);

    DynamicFormColumnBindEntity selectEditing(String formId);

    DynamicFormColumnBindEntity selectDeployed(String formId, int version);

}
