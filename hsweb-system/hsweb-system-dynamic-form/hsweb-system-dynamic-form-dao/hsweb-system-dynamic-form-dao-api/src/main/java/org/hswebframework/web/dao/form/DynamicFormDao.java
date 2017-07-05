package org.hswebframework.web.dao.form;

import org.hswebframework.web.dao.CrudDao;
import org.hswebframework.web.entity.form.DynamicFormEntity;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 动态表单 DAO接口
 *
 * @author hsweb-generator-online
 */
public interface DynamicFormDao extends CrudDao<DynamicFormEntity, String> {
    void incrementVersion(String formId);
}
