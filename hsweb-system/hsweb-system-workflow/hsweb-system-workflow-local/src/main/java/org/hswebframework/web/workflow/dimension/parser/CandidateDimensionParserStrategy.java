package org.hswebframework.web.workflow.dimension.parser;

import java.util.List;

public interface CandidateDimensionParserStrategy {
    boolean support(String type);

    List<String> parse(List<String> ids);
}
