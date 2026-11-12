package org.hswebframework.web.datasource.switcher;

/**
 * R2DBC 数据源和 Schema 的切换入口。
 *
 * @deprecated 动态切换能力已弃用，仅为已有调用保留。
 */
@Deprecated
public interface R2dbcSwitcher {
    ReactiveSwitcher datasource();

    ReactiveSwitcher schema();


}
