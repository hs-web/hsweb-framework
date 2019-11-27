package org.hswebframework.web.authorization.simple.builder;

import com.alibaba.fastjson.JSON;
import org.hswebframework.web.authorization.access.DataAccessConfig;
import org.hswebframework.web.authorization.builder.DataAccessConfigBuilder;
import org.hswebframework.web.authorization.builder.DataAccessConfigBuilderFactory;
import org.hswebframework.web.authorization.simple.*;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

import static org.hswebframework.web.authorization.access.DataAccessConfig.DefaultType.*;
import static org.hswebframework.web.authorization.access.DataAccessConfig.DefaultType.OWN_CREATED;

/**
 * @author zhouhao
 */
public class SimpleDataAccessConfigBuilderFactory implements DataAccessConfigBuilderFactory {

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
        return createConfig(supportType, (action, config) -> JSON.parseObject(config, clazz));
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
        return new SimpleDataAccessConfigBuilder(converts);
    }
}
