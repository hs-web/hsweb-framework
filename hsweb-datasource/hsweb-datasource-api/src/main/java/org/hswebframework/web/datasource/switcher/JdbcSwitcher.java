package org.hswebframework.web.datasource.switcher;

public interface JdbcSwitcher {
    Switcher datasource();

    Switcher schema();


}
