package org.hswebframework.web.service.form.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.ezorm.rdb.RDBTable;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@AllArgsConstructor
@Getter
public class FormDataQueryBeforeEvent<T> {
    private String formId;

    private RDBTable<T> table;

    private QueryParamEntity param;

}
