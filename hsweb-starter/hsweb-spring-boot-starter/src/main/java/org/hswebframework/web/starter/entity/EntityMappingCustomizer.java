package org.hswebframework.web.starter.entity;

import org.hswebframework.web.commons.entity.factory.MapperEntityFactory;

/**
 * @author zhouhao
 */
public interface EntityMappingCustomizer {
    void customize(MapperEntityFactory entityFactory);
}
