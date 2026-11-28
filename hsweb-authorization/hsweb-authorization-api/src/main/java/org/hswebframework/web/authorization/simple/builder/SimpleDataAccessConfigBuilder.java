package org.hswebframework.web.authorization.simple.builder;

import org.hswebframework.web.authorization.access.DataAccessConfig;
import org.hswebframework.web.authorization.builder.DataAccessConfigBuilder;
import org.jetlinks.json.Json;
import org.jetlinks.json.JsonMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 构建单个数据权限配置。Map 直接合并，JSON 文本由指定门面解析。
 * 保留 String 型转换器扩展：仅在调用转换器时序列化嵌套 config；未设置时传递完整配置。
 *
 * @author zhouhao
 */
public class SimpleDataAccessConfigBuilder implements DataAccessConfigBuilder {

    private final List<DataAccessConfigConverter> converts;

    private final Map<String, Object> config = new HashMap<>();

    private final JsonMapper mapper;


    public SimpleDataAccessConfigBuilder(List<DataAccessConfigConverter> converts) {
        this(converts, Json.global());
    }

    /** 复用指定配置，Map 输入不再序列化后重新解析。 */
    public SimpleDataAccessConfigBuilder(List<DataAccessConfigConverter> converts, JsonMapper mapper) {
        Objects.requireNonNull(converts);
        this.converts = converts;
        this.mapper = Objects.requireNonNull(mapper, "mapper");
    }

    @Override
    public DataAccessConfigBuilder fromJson(String json) {
        config.putAll(mapper.readAsMap(json, Object.class));
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
        String type = Objects.toString(this.config.get("type"), null);
        String action = Objects.toString(this.config.get("action"), null);
        Object configured = this.config.get("config");
        String config = configured instanceof String text ? text : mapper.write(
            configured == null ? this.config : configured);

        Objects.requireNonNull(type);
        Objects.requireNonNull(action);

        return converts.stream()
                .filter(convert -> convert.isSupport(type, action, config))
                .map(convert -> convert.convert(type, action, config))
                .findFirst()
                .orElse(null);
    }
}
