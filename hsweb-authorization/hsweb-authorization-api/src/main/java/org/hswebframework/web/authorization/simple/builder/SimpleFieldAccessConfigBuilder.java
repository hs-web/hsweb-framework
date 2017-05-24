package org.hswebframework.web.authorization.simple.builder;

import com.alibaba.fastjson.JSON;
import org.hswebframework.web.authorization.access.FieldAccessConfig;
import org.hswebframework.web.authorization.builder.FieldAccessConfigBuilder;
import org.hswebframework.web.authorization.simple.SimpleFieldAccess;

import java.util.Objects;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimpleFieldAccessConfigBuilder implements FieldAccessConfigBuilder {

    private String json;

    @Override
    public FieldAccessConfigBuilder fromJson(String json) {
        this.json = json;
        return this;
    }

    @Override
    public FieldAccessConfig build() {
        Objects.requireNonNull(json);
        return JSON.parseObject(json, SimpleFieldAccess.class);
    }
}
