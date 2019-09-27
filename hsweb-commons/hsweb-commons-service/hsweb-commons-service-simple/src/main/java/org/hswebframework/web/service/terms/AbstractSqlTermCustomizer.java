package org.hswebframework.web.service.terms;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.ezorm.rdb.metadata.RDBColumnMetadata;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.PrepareSqlFragments;

import java.util.*;

/**
 * @author zhouhao
 * @since 3.1.0
 */
@AllArgsConstructor
public abstract class AbstractSqlTermCustomizer implements SqlTermCustomizer {

    @Getter
    protected final String termType;

    @SuppressWarnings("unchecked")
    protected List<Object> convertList(Object value) {
        if (value == null) return new ArrayList<>();
        if (value instanceof List) return (List) value;
        if (value instanceof Collection) return new ArrayList<>(((Collection) value));
        if (value instanceof String) {
            String[] arr = ((String) value).split("[,]");
            Object[] objArr = new Object[arr.length];
            for (int i = 0; i < arr.length; i++) {
                String str = arr[i];
                Object val = str;
                objArr[i] = val;
            }
            return new ArrayList<>(Arrays.asList(objArr));
        } else if (value.getClass().isArray()) {
            return new ArrayList<>(Arrays.asList(((Object[]) value)));
        } else {
            return new ArrayList<>(Collections.singletonList(value));
        }
    }

    @SuppressWarnings("unchecked")
    protected List<Object> convertList(RDBColumnMetadata column, Object value) {
        if (value == null) return new ArrayList<>();
        if (value instanceof List) return (List) value;
        if (value instanceof Collection) return new ArrayList<>(((Collection) value));
        if (value instanceof String) {
            String[] arr = ((String) value).split("[,]");
            Object[] objArr = new Object[arr.length];
            for (int i = 0; i < arr.length; i++) {
                String str = arr[i];
                Object val = str;
                objArr[i] = column.encode(val);
            }
            return new ArrayList<>(Arrays.asList(objArr));
        } else if (value.getClass().isArray()) {
            return new ArrayList<>(Arrays.asList(((Object[]) value)));
        } else {
            return new ArrayList<>(Collections.singletonList(value));
        }
    }

    protected void appendCondition(PrepareSqlFragments appender,RDBColumnMetadata columnMetadata, List<Object> values) {
        int len = values.size();
        if (len == 1) {
            appender.addSql("=?").addParameter(values);
        } else {
            appender.addSql("in(");
            for (int i = 0; i < len; i++) {
                if (i > 0) {
                    appender.addSql(",");
                }
                appender.addSql("?");
            }
            appender.addSql(")").addParameter(values);
        }
    }
}