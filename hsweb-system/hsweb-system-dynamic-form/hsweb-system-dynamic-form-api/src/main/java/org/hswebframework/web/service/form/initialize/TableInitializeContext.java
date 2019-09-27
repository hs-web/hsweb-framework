package org.hswebframework.web.service.form.initialize;

import org.hswebframework.ezorm.rdb.metadata.RDBTableMetadata;
import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.hswebframework.web.entity.form.DynamicFormEntity;

/**
 * @author zhouhao
 */
public interface TableInitializeContext {

    DatabaseOperator getDatabase();

    DynamicFormEntity getFormEntity();

    RDBTableMetadata getTable();

}
