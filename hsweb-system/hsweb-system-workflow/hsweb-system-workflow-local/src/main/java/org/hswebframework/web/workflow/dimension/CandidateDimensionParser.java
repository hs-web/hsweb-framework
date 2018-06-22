package org.hswebframework.web.workflow.dimension;

/**
 * @author zhouhao
 * @see CandidateDimension
 * @since 3.0.0-RC
 */
public interface CandidateDimensionParser {
    CandidateDimension parse(DimensionContext context, String jsonConfig);
}
