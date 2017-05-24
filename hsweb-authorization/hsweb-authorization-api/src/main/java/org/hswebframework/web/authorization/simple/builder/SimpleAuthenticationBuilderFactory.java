package org.hswebframework.web.authorization.simple.builder;

import org.hswebframework.web.authorization.builder.AuthenticationBuilder;
import org.hswebframework.web.authorization.builder.AuthenticationBuilderFactory;
import org.hswebframework.web.authorization.builder.DataAccessConfigBuilderFactory;
import org.hswebframework.web.authorization.builder.FieldAccessConfigBuilderFactory;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimpleAuthenticationBuilderFactory implements AuthenticationBuilderFactory {
    private FieldAccessConfigBuilderFactory fieldBuilderFactory;

    private DataAccessConfigBuilderFactory dataBuilderFactory;

    public SimpleAuthenticationBuilderFactory(FieldAccessConfigBuilderFactory fieldBuilderFactory, DataAccessConfigBuilderFactory dataBuilderFactory) {
        this.fieldBuilderFactory = fieldBuilderFactory;
        this.dataBuilderFactory = dataBuilderFactory;
    }

    @Override
    public AuthenticationBuilder create() {
        return new SimpleAuthenticationBuilder(fieldBuilderFactory, dataBuilderFactory);
    }
}
