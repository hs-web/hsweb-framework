package org.hsweb.web.service.impl.form.trigger;

import org.webbuilder.sql.exception.TriggerException;
import org.webbuilder.sql.trigger.ScriptTriggerSupport;
import org.webbuilder.sql.trigger.TriggerResult;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhouhao on 16-5-17.
 */
public class ScriptTrigger extends ScriptTriggerSupport {
    private Map<String, ?> defaultVar = new HashMap<>();

    @Override
    public TriggerResult execute(Map<String, Object> root) throws TriggerException {
        root.putAll(defaultVar);
        return super.execute(root);
    }

    public void setDefaultVar(Map<String, ?> defaultVar) {
        this.defaultVar = defaultVar;
    }


}
