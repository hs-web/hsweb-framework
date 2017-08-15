package org.hswebframework.web.authorization.basic.define;

import org.hswebframework.web.authorization.define.Script;

/**
 * @author zhouhao
 */
public class DefaultScript implements Script {
    private String language;

    private String script;

    public DefaultScript() {
    }

    public DefaultScript(String language, String script) {
        this.language = language;
        this.script = script;
    }

    @Override
    public String getLanguage() {
        return language;
    }

    @Override
    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
