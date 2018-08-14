package org.hswebframework.web.datasource.switcher;

/**
 * 表切换器
 *
 * @author zhouhao
 * @since 3.0.0-RC
 */
public interface TableSwitcher {
    void use(String source, String target);

    String getTable(String name);

    void reset();
}
