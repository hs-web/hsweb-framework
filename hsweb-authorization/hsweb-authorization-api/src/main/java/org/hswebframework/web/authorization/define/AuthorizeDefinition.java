package org.hswebframework.web.authorization.define;


import java.util.StringJoiner;

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

    default String getDescription() {
        ResourcesDefinition res = getResources();
        StringJoiner joiner = new StringJoiner(";");
        for (ResourceDefinition resource : res.getResources()) {
            joiner.add(resource.getId() + ":" + String.join(",", resource.getActionIds()));
        }
        return joiner.toString();
    }
}
