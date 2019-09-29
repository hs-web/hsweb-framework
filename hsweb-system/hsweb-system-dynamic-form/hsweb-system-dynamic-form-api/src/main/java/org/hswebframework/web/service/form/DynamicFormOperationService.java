package org.hswebframework.web.service.form;

import org.hswebframework.ezorm.rdb.mapping.defaults.record.Record;
import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.commons.entity.param.DeleteParamEntity;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.commons.entity.param.UpdateParamEntity;

import java.util.List;
import java.util.Map;

/**
 * 动态表单操作接口,用于对动态表单进行增删改查操作
 *
 * @author zhouhao
 * @since 3.0
 */
public interface DynamicFormOperationService {
    PagerResult<Record> selectPager(String formId, QueryParamEntity paramEntity);

    Record selectSingle(String formId, QueryParamEntity paramEntity);

    List<Record> select(String formId, QueryParamEntity paramEntity);

    int count(String formId, QueryParamEntity paramEntity);

    Record updateById(String formId, String id, Map<String, Object> data);

    Record insert(String formId, Map<String, Object> entity);

    int delete(String formId, DeleteParamEntity paramEntity);

    int deleteById(String formId, String id);

      Record saveOrUpdate(String formId, Map<String,Object> data);

    Record selectById(String formId, String id);

}
