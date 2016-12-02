package org.hsweb.web.mybatis.builder;


import org.hsweb.ezorm.rdb.render.dialect.Dialect;

@Deprecated
public class MysqlParamBuilder extends DefaultSqlParamBuilder {
    private static MysqlParamBuilder instance = new MysqlParamBuilder();

    public MysqlParamBuilder() {
    }

    @Override
    public boolean filedToUpperCase() {
        return false;
    }

    @Override
    public Dialect getDialect() {
        return Dialect.MYSQL;
    }

    public static MysqlParamBuilder instance() {
        return instance;
    }

}
