package org.hswebframework.web.commons.entity.factory;

import java.util.function.Function;

/**
 * 默认的实体映射
 *
 * @author zhouhao
 */
@FunctionalInterface
public interface DefaultMapperFactory extends Function<Class, MapperEntityFactory.Mapper> {
}
