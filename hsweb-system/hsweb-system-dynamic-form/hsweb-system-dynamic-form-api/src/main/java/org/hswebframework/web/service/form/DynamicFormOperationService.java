package org.hswebframework.web.service.form;

import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.commons.entity.param.DeleteParamEntity;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.commons.entity.param.UpdateParamEntity;

import java.util.List;

/**
 * 动态表单操作接口,用于对动态表单进行增删改查操作
 *
 * @author zhouhao
 * @since 3.0
 */
public interface DynamicFormOperationService {
    <T> PagerResult<T> selectPager(String formId, QueryParamEntity paramEntity);

    <T> T selectSingle(String formId, QueryParamEntity paramEntity);

    <T> List<T> select(String formId, QueryParamEntity paramEntity);

    int count(String formId, QueryParamEntity paramEntity);

    <T> int update(String formId, UpdateParamEntity<T> paramEntity);

    <T> T updateById(String formId, Object id, T data);

    <T> T insert(String formId, T entity);

    int delete(String formId, DeleteParamEntity paramEntity);

    int deleteById(String formId, Object id);

    <T> T saveOrUpdate(String formId, T data);

    <T> T selectById(String formId, Object id);

}
