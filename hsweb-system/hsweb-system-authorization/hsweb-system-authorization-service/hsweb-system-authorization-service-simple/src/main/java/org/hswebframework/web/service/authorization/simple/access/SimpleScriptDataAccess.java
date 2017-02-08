package org.hswebframework.web.service.authorization.simple.access;

import org.hswebframework.web.authorization.access.ScriptDataAccess;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimpleScriptDataAccess extends AbstractDataAccess implements ScriptDataAccess {
    private String script;

    private String scriptLanguage;

    public SimpleScriptDataAccess() {
    }

    public SimpleScriptDataAccess(String script, String scriptLanguage) {
        this.script = script;
        this.scriptLanguage = scriptLanguage;
    }

    @Override
    public String getScriptLanguage() {
        return scriptLanguage;
    }

    public void setScriptLanguage(String scriptLanguage) {
        this.scriptLanguage = scriptLanguage;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }
}
