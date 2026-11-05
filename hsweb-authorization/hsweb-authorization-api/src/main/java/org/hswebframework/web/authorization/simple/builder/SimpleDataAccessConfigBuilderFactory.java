package org.hswebframework.web.authorization.simple.builder;

import jakarta.annotation.PostConstruct;
import org.hswebframework.web.authorization.access.DataAccessConfig;
import org.hswebframework.web.authorization.builder.DataAccessConfigBuilder;
import org.hswebframework.web.authorization.builder.DataAccessConfigBuilderFactory;
import org.hswebframework.web.authorization.simple.*;
import org.jetlinks.json.Json;
import org.jetlinks.json.JsonMapper;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

import static org.hswebframework.web.authorization.access.DataAccessConfig.DefaultType.*;
import static org.hswebframework.web.authorization.access.DataAccessConfig.DefaultType.OWN_CREATED;

/**
 * 创建共享 JSON 配置的数据权限构建器。转换器在初始化时注册，完成配置后再并发使用。
 *
 * @author zhouhao
 */
public class SimpleDataAccessConfigBuilderFactory implements DataAccessConfigBuilderFactory {
    private final JsonMapper mapper;

    public SimpleDataAccessConfigBuilderFactory() {
        this(Json.global());
    }

    /** 使用同一 JSON 配置创建数据权限构建器和默认转换器。 */
    public SimpleDataAccessConfigBuilderFactory(JsonMapper mapper) {
        this.mapper = Objects.requireNonNull(mapper, "mapper");
    }

    private List<String> defaultSupportConvert = Arrays.asList(
            OWN_CREATED,
            DIMENSION_SCOPE,
            DENY_FIELDS);

    private List<DataAccessConfigConverter> converts = new LinkedList<>();

    public SimpleDataAccessConfigBuilderFactory addConvert(DataAccessConfigConverter configBuilderConvert) {
        Objects.requireNonNull(configBuilderConvert);
        converts.add(configBuilderConvert);
        return this;
    }

    public void setDefaultSupportConvert(List<String> defaultSupportConvert) {
        this.defaultSupportConvert = defaultSupportConvert;
    }

    public List<String> getDefaultSupportConvert() {
        return defaultSupportConvert;
    }

    protected DataAccessConfigConverter createJsonConfig(String supportType, Class<? extends AbstractDataAccessConfig> clazz) {
        return createConfig(supportType, (action, config) -> mapper.read(config, clazz));
    }


    protected DataAccessConfigConverter createConfig(String supportType, BiFunction<String, String, ? extends DataAccessConfig> function) {
        return new DataAccessConfigConverter() {
            @Override
            public boolean isSupport(String type, String action, String config) {
                return supportType.equals(type);
            }

            @Override
            public DataAccessConfig convert(String type, String action, String config) {
                DataAccessConfig conf = function.apply(action, config);
                if (conf instanceof AbstractDataAccessConfig) {
                    ((AbstractDataAccessConfig) conf).setAction(action);
                }
                return conf;
            }
        };
    }

    @PostConstruct
    public void init() {


        if (defaultSupportConvert.contains(DENY_FIELDS)) {
            converts.add(createJsonConfig(DENY_FIELDS, SimpleFieldFilterDataAccessConfig.class));
        }

        if (defaultSupportConvert.contains(DIMENSION_SCOPE)) {
            converts.add(createJsonConfig(DIMENSION_SCOPE, DimensionDataAccessConfig.class));
        }

        if (defaultSupportConvert.contains(OWN_CREATED)) {
            converts.add(createConfig(OWN_CREATED, (action, config) -> new SimpleOwnCreatedDataAccessConfig(action)));
        }

    }

    @Override
    public DataAccessConfigBuilder create() {
        return new SimpleDataAccessConfigBuilder(converts, mapper);
    }
}
