package org.hswebframework.web.authorization.define;

import org.hswebframework.web.authorization.annotation.Logical;

import java.util.Set;

/**
 * 权限控制定义,定义权限控制的方式
 *
 * @author zhouhao
 * @see AuthorizeDefinitionParser
 * @since 3.0
 */
public interface AuthorizeDefinition {
    int getPriority();

    boolean isDataAccessControll();

    Set<String> getPermissions();

    Set<String> getActions();

    Set<String> getRroles();

    Set<String> getUser();

    Script getScript();

    String getMessage();

    Logical getLogical();
}
