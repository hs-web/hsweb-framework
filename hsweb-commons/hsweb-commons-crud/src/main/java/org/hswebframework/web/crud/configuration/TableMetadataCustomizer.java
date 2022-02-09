package org.hswebframework.web.crud.configuration;

import org.hswebframework.ezorm.rdb.metadata.RDBColumnMetadata;
import org.hswebframework.ezorm.rdb.metadata.RDBTableMetadata;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Set;

/**
 * 表结构自定义器,实现此接口来自定义表结构.
 *
 * @author zhouhao
 * @since 4.0.14
 */
public interface TableMetadataCustomizer {

    /**
     * 自定义列,在列被解析后调用.
     *
     * @param entityType  实体类型
     * @param descriptor  字段描述
     * @param field       字段
     * @param column      列定义
     * @param annotations 字段上的注解
     */
    void customColumn(Class<?> entityType,
                      PropertyDescriptor descriptor,
                      Field field,
                      Set<Annotation> annotations,
                      RDBColumnMetadata column);

    /**
     * 自定义表,在实体类被解析完成后调用.
     *
     * @param entityType 字段类型
     * @param table      表结构
     */
    void customTable(Class<?> entityType, RDBTableMetadata table);
}
