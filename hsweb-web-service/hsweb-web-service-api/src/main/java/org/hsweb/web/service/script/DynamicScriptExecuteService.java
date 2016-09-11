package org.hsweb.web.service.script;

import java.util.Map;

/**
 * Created by zhouhao on 16-6-29.
 */
public interface DynamicScriptExecuteService {
    Object exec(String scriptId, Map<String, Object> var) throws Throwable;

    Object exec(String name, String type, Map<String, Object> var) throws Throwable;

}
