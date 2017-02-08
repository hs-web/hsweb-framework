package org.hswebframework.web.authorization.shiro.boost.handler;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.shiro.authz.AuthorizationException;
import org.hswebframework.expands.script.engine.DynamicScriptEngine;
import org.hswebframework.expands.script.engine.DynamicScriptEngineFactory;
import org.hswebframework.web.BusinessException;
import org.hswebframework.web.authorization.access.*;
import org.hswebframwork.utils.StringUtils;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class ScriptDataAccessHandler implements DataAccessHandler {
    @Override
    public boolean isSupport(DataAccess access) {
        return access instanceof ScriptDataAccess;
    }

    @Override
    public boolean doAccess(DataAccess access, ParamContext context) {
        ScriptDataAccess dataAccess = ((ScriptDataAccess) access);
        DynamicScriptEngine engine = DynamicScriptEngineFactory.getEngine(dataAccess.getScriptLanguage());
        if (engine == null) throw new UnsupportedOperationException(dataAccess.getScriptLanguage() + " {not_support}");
        String scriptId = DigestUtils.md5Hex(dataAccess.getScript());
        try {
            if (!engine.compiled(scriptId)) {
                engine.compile(scriptId, dataAccess.getScript());
            }
            Object success = engine.execute(scriptId, context.getParams()).getIfSuccess();
            return StringUtils.isTrue(success);
        } catch (Exception e) {
            throw new BusinessException("{script_error}", e);
        }
    }

}
