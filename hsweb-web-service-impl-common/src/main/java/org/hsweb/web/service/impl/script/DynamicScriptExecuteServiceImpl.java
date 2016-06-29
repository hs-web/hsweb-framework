package org.hsweb.web.service.impl.script;

import org.hsweb.commons.MD5;
import org.hsweb.expands.script.engine.DynamicScriptEngine;
import org.hsweb.expands.script.engine.DynamicScriptEngineFactory;
import org.hsweb.expands.script.engine.ExecuteResult;
import org.hsweb.expands.script.engine.ScriptContext;
import org.hsweb.web.bean.po.script.DynamicScript;
import org.hsweb.web.core.authorize.ExpressionScopeBean;
import org.hsweb.web.core.exception.NotFoundException;
import org.hsweb.web.service.script.DynamicScriptExecuteService;
import org.hsweb.web.service.script.DynamicScriptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Map;

/**
 * Created by zhouhao on 16-6-29.
 */
@Service("dynamicScriptExecuteService")
public class DynamicScriptExecuteServiceImpl implements DynamicScriptExecuteService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired(required = false)
    protected Map<String, ExpressionScopeBean> expressionScopeBeanMap;

    @Resource
    protected DynamicScriptService dynamicScriptService;

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Object exec(String name, String type, Map<String, Object> var) throws Throwable {
        DynamicScript script = dynamicScriptService.selectByNameAndType(name, type);
        assertNotNull(script, "脚本不存在");
        return exec(script, var);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Object exec(String id, Map<String, Object> var) throws Throwable {
        if (id.contains(".")) {
            String nameAndType[] = id.split("[.]");
            return exec(nameAndType[0], nameAndType[1], var);
        }
        DynamicScript script = dynamicScriptService.selectByPk(id);
        assertNotNull(script, "脚本不存在");
        return exec(script, var);
    }
    @Transactional(rollbackFor = Throwable.class)
    protected Object exec(DynamicScript script, Map<String, Object> var) throws Throwable {
        if (script.getStatus() != 1) {
            assertNotNull(null, "脚本已禁用");
        }
        DynamicScriptEngine engine = DynamicScriptEngineFactory.getEngine(script.getType());
        assertNotNull(engine, "不支持的引擎");
        if (!engine.compiled(script.getId())) {
            dynamicScriptService.compile(script.getId());
        }
        if (expressionScopeBeanMap != null) {
            var.putAll(expressionScopeBeanMap);
        }
        ScriptContext context = engine.getContext(script.getId());
        //如果发生了变化,自动重新进行编译
        if (!context.getMd5().equals(MD5.defaultEncode(script.getContent()))) {
            dynamicScriptService.compile(script.getId());
        }
        ExecuteResult result = engine.execute(script.getId(), var);
        if (!result.isSuccess()) {
            if (result.getException() != null)
                throw result.getException();
        }
        return result.getResult();
    }

    protected void assertNotNull(Object po, String message) {
        if (po == null) throw new NotFoundException(message);
    }

}
