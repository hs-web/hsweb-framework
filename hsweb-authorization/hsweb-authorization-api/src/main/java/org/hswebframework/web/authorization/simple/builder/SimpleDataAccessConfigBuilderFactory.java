package org.hswebframework.web.authorization.simple.builder;

import com.alibaba.fastjson.JSON;
import org.hswebframework.web.authorization.access.DataAccessConfig;
import org.hswebframework.web.authorization.builder.DataAccessConfigBuilder;
import org.hswebframework.web.authorization.builder.DataAccessConfigBuilderFactory;
import org.hswebframework.web.authorization.simple.SimpleCustomDataAccess;
import org.hswebframework.web.authorization.simple.SimpleOwnCreatedDataAccess;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimpleDataAccessConfigBuilderFactory implements DataAccessConfigBuilderFactory {
    private List<DataAccessConfigBuilderConvert> converts = new LinkedList<>();

    public SimpleDataAccessConfigBuilderFactory addConvert(DataAccessConfigBuilderConvert configBuilderConvert) {
        Objects.requireNonNull(configBuilderConvert);
        converts.add(configBuilderConvert);
        return this;
    }

    public SimpleDataAccessConfigBuilderFactory() {
        converts.add(new DataAccessConfigBuilderConvert() {
            @Override
            public boolean isSupport(String type, String action, String config) {
                return DataAccessConfig.DefaultType.OWN_CREATED.equals(type);
            }

            @Override
            public DataAccessConfig convert(String type, String action, String config) {
                return new SimpleOwnCreatedDataAccess(action);
            }
        });

        converts.add(new DataAccessConfigBuilderConvert() {
            @Override
            public boolean isSupport(String type, String action, String config) {
                return DataAccessConfig.DefaultType.SCRIPT.equals(type);
            }

            @Override
            public DataAccessConfig convert(String type, String action, String config) {
                SimpleOwnCreatedDataAccess access = JSON.parseObject(config, SimpleOwnCreatedDataAccess.class);
                access.setAction(config);
                return access;
            }
        });
        converts.add(new DataAccessConfigBuilderConvert() {
            @Override
            public boolean isSupport(String type, String action, String config) {
                return DataAccessConfig.DefaultType.CUSTOM.equals(type);
            }

            @Override
            public DataAccessConfig convert(String type, String action, String config) {
                SimpleCustomDataAccess access = new SimpleCustomDataAccess(config);
                access.setAction(action);
                return access;
            }
        });
    }

    @Override
    public DataAccessConfigBuilder create() {
        return new SimpleDataAccessConfigBuilder(converts);
    }
}
