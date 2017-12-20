package org.hswebframework.web.datasource.config;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class InSpringDynamicDataSourceConfig extends DynamicDataSourceConfig {
    private static final long serialVersionUID = -8434216403009495774L;

    private String beanName;
}
