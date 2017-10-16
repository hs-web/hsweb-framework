package org.hswebframework.web.authorization.access;

/**
 * 通过脚本来控制数据操作权限.脚本可以在前端设置角色的时候进行编辑
 *
 * @author zhouhao
 */
public interface ScriptDataAccessConfig extends DataAccessConfig {
    @Override
    default String getType() {
        return DefaultType.SCRIPT;
    }

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
