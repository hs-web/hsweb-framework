package org.hswebframework.web.organizational.authorization.relation;

import java.io.Serializable;

/**
 * @author zhouhao
 * @since 3.0
 */
public interface RelationTargetSupplier {

    boolean support(String targetType, String target);

    Serializable get(String target);
}
