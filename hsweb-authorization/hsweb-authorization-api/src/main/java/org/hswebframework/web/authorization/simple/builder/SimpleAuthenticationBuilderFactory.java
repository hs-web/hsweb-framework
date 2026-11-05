package org.hswebframework.web.authorization.simple.builder;

import org.hswebframework.web.authorization.builder.AuthenticationBuilder;
import org.hswebframework.web.authorization.builder.AuthenticationBuilderFactory;
import org.hswebframework.web.authorization.builder.DataAccessConfigBuilderFactory;
import org.jetlinks.json.Json;
import org.jetlinks.json.JsonMapper;

import java.util.Objects;

/**
 * 按次创建认证构建器，复用数据权限工厂和已配置的 JSON 门面。
 *
 * @author zhouhao
 */
public class SimpleAuthenticationBuilderFactory implements AuthenticationBuilderFactory {

    private DataAccessConfigBuilderFactory dataBuilderFactory;

    private final JsonMapper mapper;

    public SimpleAuthenticationBuilderFactory(DataAccessConfigBuilderFactory dataBuilderFactory) {
        this(dataBuilderFactory, Json.global());
    }

    /** 需要应用自定义绑定规则时显式传入门面，旧构造方法使用 facade 应用默认配置。 */
    public SimpleAuthenticationBuilderFactory(DataAccessConfigBuilderFactory dataBuilderFactory, JsonMapper mapper) {
        this.dataBuilderFactory = dataBuilderFactory;
        this.mapper = Objects.requireNonNull(mapper, "mapper");
    }

    @Override
    public AuthenticationBuilder create() {
        return new SimpleAuthenticationBuilder(dataBuilderFactory, mapper);
    }
}
