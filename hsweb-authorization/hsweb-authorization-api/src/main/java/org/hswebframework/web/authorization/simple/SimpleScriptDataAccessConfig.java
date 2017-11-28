package org.hswebframework.web.authorization.simple;

import lombok.*;
import org.hswebframework.web.authorization.access.ScriptDataAccessConfig;

/**
 * @author zhouhao
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimpleScriptDataAccessConfig extends AbstractDataAccessConfig implements ScriptDataAccessConfig {

    private static final long serialVersionUID = 2667127339980983720L;

    private String script;

    private String scriptLanguage;
}
