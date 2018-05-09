package org.hswebframework.web.authorization.simple.builder;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.hswebframework.web.authorization.access.DataAccessConfig;
import org.hswebframework.web.authorization.builder.DataAccessConfigBuilder;

import java.util.List;
import java.util.Objects;

/**
 * @author zhouhao
 */
public class SimpleDataAccessConfigBuilder implements DataAccessConfigBuilder {
    private String json;

    private List<DataAccessConfigConvert> converts;

    public SimpleDataAccessConfigBuilder(List<DataAccessConfigConvert> converts) {
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

        if (config == null) {
            config = json;
        }
        String finalConfig = config;

        return converts.stream()
                .filter(convert -> convert.isSupport(type, action, finalConfig))
                .map(convert -> convert.convert(type, action, finalConfig))
                .findFirst()
                .orElse(null);
    }
}
