package org.hswebframework.web.datasource.switcher;

/**
 * 默认 R2DBC 数据源和 Schema 切换入口。
 *
 * @deprecated 动态切换能力已弃用，仅为已有调用保留。
 */
@Deprecated
public class DefaultR2dbcSwicher implements R2dbcSwitcher {

    private DefaultReactiveSwitcher datasourceSwitcher=new DefaultReactiveSwitcher("r2dbc-datasource","datasource");
    private DefaultReactiveSwitcher schemaSwitcher=new DefaultReactiveSwitcher("r2dbc-schema","schema");


    @Override
    public ReactiveSwitcher datasource() {
        return datasourceSwitcher;
    }

    @Override
    public ReactiveSwitcher schema() {
        return schemaSwitcher;
    }
}
