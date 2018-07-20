package org.hswebframework.web.workflow.dimension;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.vavr.Lazy;
import org.apache.commons.collections.CollectionUtils;
import org.hswebframework.web.workflow.dimension.parser.CandidateDimensionParserStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DefaultCandidateDimensionParser implements CandidateDimensionParser {

    @Autowired(required = false)
    private List<CandidateDimensionParserStrategy> strategies;

    private CandidateDimension parse(DimensionContext context, JSONArray jsonConfig) {
        List<CandidateDimensionParserStrategy.StrategyConfig> configList = jsonConfig.stream()
                .filter(json -> json instanceof JSONObject)
                .map(JSONObject.class::cast)
                .filter(json -> json.get("dimension") != null && CollectionUtils.isNotEmpty(json.getJSONArray("idList")))
                .map(json -> {
                    CandidateDimensionParserStrategy.StrategyConfig config = json.toJavaObject(CandidateDimensionParserStrategy.StrategyConfig.class);
                    if (config.getConfig() == null) {
                        config.setConfig(json);
                    }
                    return config;
                }).collect(Collectors.toList());

        if (configList.isEmpty()) {
            return CandidateDimension.empty;
        }
        return Lazy.val(() -> {
            List<String> list = configList.stream()
                    .flatMap(config ->
                            strategies
                                    .stream()
                                    .filter(strategy -> strategy.support(config.getDimension()))
                                    .map(strategy -> strategy.parse(context, config))
                                    .filter(CollectionUtils::isNotEmpty)
                                    .flatMap(Collection::stream)
                                    .filter(StringUtils::hasText)
                    ).collect(Collectors.toList());

            return (CandidateDimension) () -> list;
        }, CandidateDimension.class);

    }

    @Override
    public CandidateDimension parse(DimensionContext context, String jsonConfig) {
        JSONArray jsonArray;
        if (jsonConfig.startsWith("[")) {
            jsonArray = JSON.parseArray(jsonConfig);
        } else {
            JSONObject jsonObject = JSON.parseObject(jsonConfig);
            jsonArray = new JSONArray();
            jsonArray.add(jsonObject);
        }
        return parse(context, jsonArray);

    }
}
