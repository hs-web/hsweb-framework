package org.hswebframework.web.workflow.dao.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@Getter
@Setter
public class ListenerConfig {
    private String eventType;

    private String language;

    private String script;

}
