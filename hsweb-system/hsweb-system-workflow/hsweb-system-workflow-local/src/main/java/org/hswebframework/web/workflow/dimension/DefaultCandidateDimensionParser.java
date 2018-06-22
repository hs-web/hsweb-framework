package org.hswebframework.web.workflow.dimension;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.vavr.Lazy;
import org.apache.commons.collections.CollectionUtils;
import org.hswebframework.web.workflow.dimension.parser.CandidateDimensionParserStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DefaultCandidateDimensionParser implements CandidateDimensionParser {

    @Autowired(required = false)
    private List<CandidateDimensionParserStrategy> strategies;

    @Override
    public CandidateDimension parse(String jsonConfig) {
        JSONObject jsonObject = JSON.parseObject(jsonConfig);
        String type = jsonObject.getString("type");
        CandidateDimensionParserStrategy.StrategyConfig config = jsonObject
                .toJavaObject(CandidateDimensionParserStrategy.StrategyConfig.class);
        if (config.getConfig() == null) {
            config.setConfig(jsonObject);
        }

        if (StringUtils.isEmpty(type)
                || CollectionUtils.isEmpty(strategies)
                || CollectionUtils.isEmpty(config.getIdList())) {
            return CandidateDimension.empty;
        }

        return Lazy.val(() -> {
                    List<String> list = strategies
                            .stream()
                            .filter(strategy -> strategy.support(type))
                            .map(strategy -> strategy.parse(config))
                            .filter(CollectionUtils::isNotEmpty)
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList());
                    return (CandidateDimension) () -> list;
                }
                , CandidateDimension.class);

    }
}
