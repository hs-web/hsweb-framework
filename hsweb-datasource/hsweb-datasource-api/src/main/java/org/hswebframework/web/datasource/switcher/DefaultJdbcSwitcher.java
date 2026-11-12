package org.hswebframework.web.datasource.switcher;

/**
 * 默认 JDBC 数据源和 Schema 切换入口。
 *
 * @deprecated 动态切换能力已弃用，仅为已有调用保留。
 */
@Deprecated
public class DefaultJdbcSwitcher implements JdbcSwitcher{

    private DefaultSwitcher datasourceSwitcher=new DefaultSwitcher("jdbc-datasource","datasource");
    private DefaultSwitcher schemaSwitcher=new DefaultSwitcher("jdbc-schema","schema");

    @Override
    public Switcher datasource() {
        return datasourceSwitcher;
    }

    @Override
    public Switcher schema() {
        return schemaSwitcher;
    }
}
