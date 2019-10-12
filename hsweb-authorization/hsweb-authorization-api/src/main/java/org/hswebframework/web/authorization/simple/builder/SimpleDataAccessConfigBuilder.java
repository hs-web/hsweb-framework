package org.hswebframework.web.authorization.simple.builder;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.hswebframework.web.authorization.access.DataAccessConfig;
import org.hswebframework.web.authorization.builder.DataAccessConfigBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author zhouhao
 */
public class SimpleDataAccessConfigBuilder implements DataAccessConfigBuilder {

    private List<DataAccessConfigConvert> converts;

    private Map<String, Object> config = new HashMap<>();


    public SimpleDataAccessConfigBuilder(List<DataAccessConfigConvert> converts) {
        Objects.requireNonNull(converts);
        this.converts = converts;
    }

    @Override
    public DataAccessConfigBuilder fromJson(String json) {
        config.putAll(JSON.parseObject(json));
        return this;
    }

    @Override
    public DataAccessConfigBuilder fromMap(Map<String, Object> map) {
        config.putAll(map);
        return this;
    }

    @Override
    public DataAccessConfig build() {
        Objects.requireNonNull(config);
        JSONObject jsonObject = new JSONObject(config);

        String type = jsonObject.getString("type");
        String action = jsonObject.getString("action");
        String config = jsonObject.getString("config");

        Objects.requireNonNull(type);
        Objects.requireNonNull(action);

        if (config == null) {
            config = jsonObject.toJSONString();
        }
        String finalConfig = config;

        return converts.stream()
                .filter(convert -> convert.isSupport(type, action, finalConfig))
                .map(convert -> convert.convert(type, action, finalConfig))
                .findFirst()
                .orElse(null);
    }
}
