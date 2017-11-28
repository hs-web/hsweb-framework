package org.hswebframework.web.authorization.basic.handler.access;

import org.apache.commons.codec.digest.DigestUtils;
import org.hswebframework.expands.script.engine.DynamicScriptEngine;
import org.hswebframework.expands.script.engine.DynamicScriptEngineFactory;
import org.hswebframework.utils.StringUtils;
import org.hswebframework.web.BusinessException;
import org.hswebframework.web.authorization.access.DataAccessConfig;
import org.hswebframework.web.authorization.access.DataAccessHandler;
import org.hswebframework.web.authorization.access.ScriptDataAccessConfig;
import org.hswebframework.web.authorization.define.AuthorizingContext;

/**
 * @author zhouhao
 */
public class ScriptDataAccessHandler implements DataAccessHandler {
    @Override
    public boolean isSupport(DataAccessConfig access) {
        return access instanceof ScriptDataAccessConfig;
    }

    @Override
    public boolean handle(DataAccessConfig access, AuthorizingContext context) {
        ScriptDataAccessConfig dataAccess = ((ScriptDataAccessConfig) access);
        DynamicScriptEngine engine = DynamicScriptEngineFactory.getEngine(dataAccess.getScriptLanguage());
        if (engine == null) {
            throw new UnsupportedOperationException(dataAccess.getScriptLanguage() + " {not_support}");
        }
        String scriptId = DigestUtils.md5Hex(dataAccess.getScript());
        try {
            if (!engine.compiled(scriptId)) {
                engine.compile(scriptId, dataAccess.getScript());
            }
            Object success = engine.execute(scriptId, context.getParamContext().getParams()).getIfSuccess();
            return StringUtils.isTrue(success);
        } catch (Exception e) {
            throw new BusinessException("{script_error}", e);
        }
    }

}
