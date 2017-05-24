package org.hswebframework.web.authorization.simple.builder;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.hswebframework.web.authorization.access.DataAccessConfig;
import org.hswebframework.web.authorization.builder.DataAccessConfigBuilder;
import org.hswebframework.web.authorization.simple.SimpleCustomDataAccess;
import org.hswebframework.web.authorization.simple.SimpleOwnCreatedDataAccess;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @author zhouhao
 */
public class SimpleDataAccessConfigBuilder implements DataAccessConfigBuilder {
    private String json;

    private List<DataAccessConfigBuilderConvert> converts;

    public SimpleDataAccessConfigBuilder(List<DataAccessConfigBuilderConvert> converts) {
        Objects.requireNonNull(converts);
        this.converts = converts;
    }

    @Override
    public DataAccessConfigBuilder fromJson(String json) {
        this.json = json;
        return this;
    }

    @Override
    public DataAccessConfig build() {
        Objects.requireNonNull(json);
        JSONObject jsonObject = JSON.parseObject(json);

        String type = jsonObject.getString("type");
        String action = jsonObject.getString("action");
        String config = jsonObject.getString("config");

        Objects.requireNonNull(type);
        Objects.requireNonNull(action);


        return converts.stream().filter(convert -> convert.isSupport(type, action, config))
                .findAny().map(convert -> convert.convert(type, action, config))
                .orElse(null);
    }
}
