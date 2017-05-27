package org.hswebframework.web.authorization.simple.builder;

import org.hswebframework.web.authorization.builder.AuthenticationBuilder;
import org.hswebframework.web.authorization.builder.AuthenticationBuilderFactory;
import org.hswebframework.web.authorization.builder.DataAccessConfigBuilderFactory;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimpleAuthenticationBuilderFactory implements AuthenticationBuilderFactory {

    private DataAccessConfigBuilderFactory dataBuilderFactory;

    public SimpleAuthenticationBuilderFactory(DataAccessConfigBuilderFactory dataBuilderFactory) {
        this.dataBuilderFactory = dataBuilderFactory;
    }

    @Override
    public AuthenticationBuilder create() {
        return new SimpleAuthenticationBuilder(dataBuilderFactory);
    }
}
