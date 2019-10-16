package org.hswebframework.web.authorization.define;


/**
 * 权限控制定义,定义权限控制的方式
 *
 * @author zhouhao
 * @since 3.0
 */
public interface AuthorizeDefinition {

    ResourcesDefinition getResources();

    DimensionsDefinition getDimensions();

    String getMessage();

    Phased getPhased();

    boolean isEmpty();
}
