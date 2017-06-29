package org.hswebframework.web.service.form;

import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.commons.entity.param.DeleteParamEntity;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.commons.entity.param.UpdateParamEntity;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface DynamicFormOperationService {
    <T> PagerResult<T> selectPager(String formId, QueryParamEntity paramEntity);

    <T> T selectSingle(String formId, QueryParamEntity paramEntity);

    int count(String formId, QueryParamEntity paramEntity);

    <T> int update(String formId, UpdateParamEntity<T> paramEntity);

    int delete(String formId, DeleteParamEntity paramEntity);

}
