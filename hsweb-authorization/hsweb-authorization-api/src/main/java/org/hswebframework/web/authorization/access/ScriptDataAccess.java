package org.hswebframework.web.authorization.access;

/**
 * 动态脚本数据权限控制
 *
 * @author zhouhao
 */
public interface ScriptDataAccess extends DataAccess {

    /**
     * 脚本语言: javascript(js),groovy
     *
     * @return 语言
     */
    String getScriptLanguage();

    /**
     * 脚本内容,在进行验证的时候会执行脚本
     *
     * @return 脚本
     */
    String getScript();

}
