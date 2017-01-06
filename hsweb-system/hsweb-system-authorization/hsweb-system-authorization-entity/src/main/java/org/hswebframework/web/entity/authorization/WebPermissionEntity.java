package org.hswebframework.web.entity.authorization;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface WebPermissionEntity<A extends ActionEntity> extends PermissionEntity<A> {
    String getUri();

    void setUri(String uri);

    String getIcon();

    void setIcon(String icon);

}
