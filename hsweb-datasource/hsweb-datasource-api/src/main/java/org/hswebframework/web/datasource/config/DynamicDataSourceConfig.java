package org.hswebframework.web.datasource.config;

import lombok.Data;

import java.io.Serializable;

@Data
public class DynamicDataSourceConfig implements Serializable {
    private String id;

    private String name;

    private String describe;
}
