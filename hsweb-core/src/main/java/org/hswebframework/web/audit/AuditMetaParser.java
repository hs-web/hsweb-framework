package org.hswebframework.web.audit;

import java.util.Optional;

/**
 * @author zhouhao
 * @since 3.0.7
 */
public interface AuditMetaParser {

    Optional<AuditMeta> parse(Class<?> type);
}
