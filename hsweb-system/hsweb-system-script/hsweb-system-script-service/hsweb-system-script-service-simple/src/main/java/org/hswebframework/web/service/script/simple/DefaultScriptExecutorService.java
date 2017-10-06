package org.hswebframework.web.service.script.simple;

import org.apache.commons.codec.digest.DigestUtils;
import org.hswebframework.expands.script.engine.DynamicScriptEngine;
import org.hswebframework.expands.script.engine.DynamicScriptEngineFactory;
import org.hswebframework.expands.script.engine.ScriptContext;
import org.hswebframework.web.entity.script.ScriptEntity;
import org.hswebframework.web.service.script.ScriptExecutorService;
import org.hswebframework.web.service.script.ScriptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * @author zhouhao
 */
@Service("scriptExecutorService")
public class DefaultScriptExecutorService implements ScriptExecutorService {

    @Autowired
    private ScriptService scriptService;

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Object execute(String id, Map<String, Object> parameters) throws Exception {
        ScriptEntity scriptEntity = scriptService.selectByPk(id);

        DynamicScriptEngine engine = DynamicScriptEngineFactory.getEngine(scriptEntity.getLanguage());

        String scriptId = "dynamicScript-" + id;
        String scriptMd5 = DigestUtils.md5Hex(scriptEntity.getScript());

        ScriptContext context = engine.getContext(scriptId);

        if (context == null || !context.getMd5().equals(scriptMd5)) {
            engine.compile(scriptId, scriptEntity.getScript());
        }

        return engine.execute(scriptId, parameters).getIfSuccess();
    }
}
