package org.hswebframework.web.crud.events;

import org.hswebframework.ezorm.rdb.mapping.EntityColumnMapping;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.NativeSql;

import java.util.Map;

public interface SqlExpressionInvoker {

    Object invoke(NativeSql sql, EntityColumnMapping mapping, Map<String,Object> object);

}
