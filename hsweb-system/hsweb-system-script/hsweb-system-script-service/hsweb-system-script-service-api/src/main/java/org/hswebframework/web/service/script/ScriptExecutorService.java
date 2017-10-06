package org.hswebframework.web.service.script;

import java.util.Map;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface ScriptExecutorService {
    Object execute(String id, Map<String, Object> parameters) throws Exception;
}
