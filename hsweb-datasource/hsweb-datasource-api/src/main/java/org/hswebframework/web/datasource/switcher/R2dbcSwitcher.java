package org.hswebframework.web.datasource.switcher;

public interface R2dbcSwitcher {
    ReactiveSwitcher datasource();

    ReactiveSwitcher schema();


}
