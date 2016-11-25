package org.hsweb.web.mybatis.utils;

import java.util.LinkedList;

/**
 * @see org.hsweb.ezorm.rdb.render.SqlAppender
 * @deprecated
 */
public class SqlAppender extends LinkedList<String> {


    public SqlAppender add(Object... str) {
        for (Object s : str) {
            this.add(String.valueOf(s));
        }
        return this;
    }

    public SqlAppender addEdSpc(String... str) {
        for (String s : str) {
            this.add(s);
        }
        this.add(" ");
        return this;
    }

    /**
     * 接入sql语句，并自动加入空格
     *
     * @param str
     * @return
     */
    public SqlAppender addSpc(String... str) {
        for (String s : str) {
            this.add(s);
            this.add(" ");
        }
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        this.forEach(builder::append);
        return builder.toString();
    }

}
