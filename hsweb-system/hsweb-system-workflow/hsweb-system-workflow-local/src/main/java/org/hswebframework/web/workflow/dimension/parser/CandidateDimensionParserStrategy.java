package org.hswebframework.web.workflow.dimension.parser;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.workflow.dimension.DimensionContext;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CandidateDimensionParserStrategy {
    String DIMENSION_USER       = "user";
    String DIMENSION_ROLE       = "role";
    String DIMENSION_POSITION   = "position";
    String DIMENSION_DEPARTMENT = "department";
    String DIMENSION_ORG        = "org";
    String DIMENSION_RELATION   = "relation";

    boolean support(String dimension);

    List<String> parse(DimensionContext context, StrategyConfig config);

    @Getter
    @Setter
    class StrategyConfig {
        private String       dimension;

        private List<String> idList;

        private Map<String, Object> config;

        public Optional<Object> getConfig(String name) {
            return config == null ? Optional.empty() : Optional.ofNullable(config.get(name));
        }

        public Optional<String> getStringConfig(String name) {
            return config == null ?
                    Optional.empty() :
                    Optional.ofNullable(config.get(name))
                            .map(String::valueOf);
        }
    }
}
