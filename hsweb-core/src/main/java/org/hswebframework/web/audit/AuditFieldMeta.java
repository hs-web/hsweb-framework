package org.hswebframework.web.audit;

import lombok.Getter;
import lombok.Setter;

/**
 * @author zhouhao
 * @since 3.0.7
 */
@Getter
@Setter
public class AuditFieldMeta {
    private String field;

    private String comment;

    private String type;

    private Strategy strategy;
}
