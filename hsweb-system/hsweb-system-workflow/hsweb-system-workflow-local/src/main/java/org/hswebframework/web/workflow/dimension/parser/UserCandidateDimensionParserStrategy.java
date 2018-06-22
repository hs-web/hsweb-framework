package org.hswebframework.web.workflow.dimension.parser;

import org.hswebframework.web.workflow.dimension.DimensionContext;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@Component
public class UserCandidateDimensionParserStrategy implements CandidateDimensionParserStrategy {

    @Override
    public boolean support(String dimension) {
        return DIMENSION_USER.equals(dimension);
    }

    @Override
    public List<String> parse(DimensionContext context, StrategyConfig config) {
        return config.getIdList();
    }
}
