package org.hsweb.web.mybatis.builder;

/**
 * Created by zhouhao on 16-5-9.
 */
public class MysqlParamBuilder extends DefaultSqlParamBuilder {
    private static MysqlParamBuilder instance = new MysqlParamBuilder();

    public MysqlParamBuilder() {
    }


    public static MysqlParamBuilder instance() {
        return instance;
    }
}
