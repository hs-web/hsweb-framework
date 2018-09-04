package org.hswebframework.web.service.datasource.simple;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.datasource.config.DynamicDataSourceConfig;

import java.util.Map;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@Getter
@Setter
public class InDBDynamicDataSourceConfig extends DynamicDataSourceConfig {

    private static final long serialVersionUID = 89025460456111917L;

    private Map<String, Object> properties;

    @Override
    public boolean equals(Object o) {
        if (o instanceof InDBDynamicDataSourceConfig) {
            return o.hashCode() == hashCode();
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return properties == null ? 0 : properties.hashCode();
    }
}
