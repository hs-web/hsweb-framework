package org.hswebframework.web.service.form.initialize;

import org.hsweb.ezorm.rdb.RDBDatabase;
import org.hsweb.ezorm.rdb.meta.RDBTableMetaData;
import org.hswebframework.web.entity.form.DynamicFormEntity;

/**
 * @author zhouhao
 */
public interface TableInitializeContext {

    RDBDatabase getDatabase();

    DynamicFormEntity getFormEntity();

    RDBTableMetaData getTable();

}
