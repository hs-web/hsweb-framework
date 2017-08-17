package org.hswebframework.web.authorization.simple;

import org.hswebframework.web.authorization.access.FieldFilterDataAccessConfig;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.hswebframework.web.authorization.access.DataAccessConfig.DefaultType.DENY_FIELDS;

/**
 * 默认配置实现
 *
 * @author zhouhao
 * @see FieldFilterDataAccessConfig
 * @since 3.0
 */
public class SimpleFieldFilterDataAccessConfig extends AbstractDataAccessConfig implements FieldFilterDataAccessConfig {
    private Set<String> fields;

    public SimpleFieldFilterDataAccessConfig() {
    }

    public SimpleFieldFilterDataAccessConfig(String... fields) {
        this.fields = new HashSet<>(Arrays.asList(fields));
    }

    @Override
    public Set<String> getFields() {
        return fields;
    }

    public void setFields(Set<String> fields) {
        this.fields = fields;
    }

    @Override
    public String getType() {
        return DENY_FIELDS;
    }
}
