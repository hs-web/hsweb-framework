package org.hswebframework.web.datasource.switcher;

public interface DatabaseSwitcher {
    /**
     * 使用上一次调用的数据源
     */
    void useLast();

    /**
     * @param database 数据库名称
     */
    void use(String database);

    /**
     * 切换为默认数据库
     */
    void useDefault();

    /**
     * @return 当前选择的数据库
     */
    String currentDatabase();

    /**
     * 重置切换记录,重置后,使用默认数据库
     */
    void reset();
}
