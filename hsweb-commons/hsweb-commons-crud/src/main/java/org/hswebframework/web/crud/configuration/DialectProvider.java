package org.hswebframework.web.crud.configuration;

import org.hswebframework.ezorm.rdb.metadata.RDBSchemaMetadata;
import org.hswebframework.ezorm.rdb.metadata.dialect.Dialect;

/**
 * 数据库方言提供商, 通过实现此接口拓展数据库方言.
 * <p>
 * 实现此接口,并使用jdk SPI暴露实现.
 * <pre>{@code
 *   META-INF/services/org.hswebframework.web.crud.configuration.DialectProvider
 * }</pre>
 *
 * @author zhouhao
 * @see java.util.ServiceLoader
 * @since 4.0.17
 */
public interface DialectProvider {

    /**
     * 方言名称
     *
     * @return 方言名称
     */
    String name();

    /**
     * 获取方言实例
     *
     * @return 方言实例
     */
    Dialect getDialect();

    /**
     * 获取sql预编译参数绑定符号，如: ?
     *
     * @return 参数绑定符号
     */
    String getBindSymbol();

    /**
     * 创建一个schema
     *
     * @param name schema名称
     * @return schema
     */
    RDBSchemaMetadata createSchema(String name);

    /**
     * 获取验证连接的sql
     *
     * @return sql
     */
    default String getValidationSql() {
        return "select 1";
    }
}
