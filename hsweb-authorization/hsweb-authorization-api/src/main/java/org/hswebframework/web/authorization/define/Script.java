package org.hswebframework.web.authorization.define;


/**
 * 使用脚本进行权限控制
 *
 * @author zhouhao
 * @since 3.0
 */
public interface Script {
    /**
     * @return 脚本语言, js,groovy,spel等
     */
    String getLanguage();

    /**
     * js:
     * <pre>
     *    return auth.hasRole("admin");
     * </pre>
     *
     * @return 脚本内容
     */
    String getScript();

}
