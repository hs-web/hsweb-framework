package org.hswebframework.web.datasource.switcher;

/**
 * 表切换器
 *
 * @author zhouhao
 * @since 3.0.0-RC
 * @deprecated 动态切换能力已弃用，仅为已有调用保留。
 */
@Deprecated
public interface TableSwitcher {
    void use(String source, String target);

    String getTable(String name);

    void reset();
}
