package org.hswebframework.web.service.form;

import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.commons.entity.param.DeleteParamEntity;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.commons.entity.param.UpdateParamEntity;

import java.util.List;

/**
 * 动态表单操作接口,用于对动态表单进行增删改查操作
 *
 * @since 3.0
 * @author zhouhao
 *
 */
public interface DynamicFormOperationService {
    <T> PagerResult<T> selectPager(String formId, QueryParamEntity paramEntity);

    <T> T selectSingle(String formId, QueryParamEntity paramEntity);

    <T> List<T> select(String formId, QueryParamEntity paramEntity);

    int count(String formId, QueryParamEntity paramEntity);

    <T> int update(String formId, UpdateParamEntity<T> paramEntity);

    <T> void insert(String formId, T entity);

    int delete(String formId, DeleteParamEntity paramEntity);


}
