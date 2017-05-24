package org.hswebframework.web.authorization.simple;

import org.hswebframework.web.authorization.access.ScriptDataAccessConfig;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimpleScriptDataAccessConfig extends AbstractDataAccessConfig implements ScriptDataAccessConfig {
    private String script;

    private String scriptLanguage;

    public SimpleScriptDataAccessConfig() {
    }

    public SimpleScriptDataAccessConfig(String script, String scriptLanguage) {
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
