package org.hswebframework.web.datasource.switcher;

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
