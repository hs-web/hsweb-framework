package org.hswebframework.web.service.form.initialize;

import org.hswebframework.ezorm.rdb.meta.RDBColumnMetaData;
import org.hswebframework.web.entity.form.DynamicFormColumnEntity;

/**
 * @author zhouhao
 */
public interface ColumnInitializeContext extends TableInitializeContext {
    DynamicFormColumnEntity getColumnEntity();

    RDBColumnMetaData getColumn();
}
