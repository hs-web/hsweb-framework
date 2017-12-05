package org.hswebframework.web.service.script;

import java.util.Map;

/**
 *
 * @author zhouhao
 */
public interface ScriptExecutorService {
    Object execute(String id, Map<String, Object> parameters) throws Exception;
}
