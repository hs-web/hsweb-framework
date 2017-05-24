package org.hswebframework.web.authorization.simple.builder;

import org.hswebframework.web.authorization.builder.FieldAccessConfigBuilder;
import org.hswebframework.web.authorization.builder.FieldAccessConfigBuilderFactory;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimpleFieldAccessConfigBuilderFactory implements FieldAccessConfigBuilderFactory {
    @Override
    public FieldAccessConfigBuilder create() {
        return new SimpleFieldAccessConfigBuilder();
    }
}
