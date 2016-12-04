/*
 * Copyright 2015-2016 http://hsweb.me
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hsweb.web.bean.po.datasource;

import org.hibernate.validator.constraints.NotBlank;
import org.hsweb.web.bean.po.GenericPo;

/**
 * 数据源
 * Created by hsweb-generator 2016-8-23 15:52:11
 */
public class DataSource extends GenericPo<String> {
    //数据源名称
    @NotBlank
    private String         name;
    //url
    @NotBlank
    private String         url;
    //用户名
    @NotBlank
    private String         username;
    //测试sql
    private String         testSql;
    //密码
    private String         password;
    //是否启用
    private int            enabled;
    //创建日期
    private java.util.Date createDate;

    /**
     * 获取 数据源名称
     *
     * @return String 数据源名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 设置 数据源名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取 用户名
     *
     * @return String 用户名
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * 设置 用户名
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取 密码
     *
     * @return String 密码
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * 设置 密码
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 获取 是否启用
     *
     * @return int 是否启用
     */
    public int getEnabled() {
        return this.enabled;
    }

    /**
     * 设置 是否启用
     */
    public void setEnabled(int enabled) {
        this.enabled = enabled;
    }

    /**
     * 获取 创建日期
     *
     * @return java.util.Date 创建日期
     */
    public java.util.Date getCreateDate() {
        return this.createDate;
    }

    /**
     * 设置 创建日期
     */
    public void setCreateDate(java.util.Date createDate) {
        this.createDate = createDate;
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTestSql() {
        return testSql;
    }

    public void setTestSql(String testSql) {
        this.testSql = testSql;
    }

    public int getHash() {
        StringBuilder builder = new StringBuilder();
        builder.append(url).append(username).append(password).append(enabled);
        return builder.toString().hashCode();
    }


    public interface Property extends GenericPo.Property {
        /**
         * @see DataSource#name
         */
        String name       = "name";
        /**
         * @see DataSource#url
         */
        String url        = "url";
        /**
         * @see DataSource#username
         */
        String username   = "username";
        /**
         * @see DataSource#testSql
         */
        String testSql    = "testSql";
        /**
         * @see DataSource#password
         */
        String password   = "password";
        /**
         * @see DataSource#enabled
         */
        String enabled    = "enabled";
        /**
         * @see DataSource#createDate
         */
        String createDate = "createDate";
    }
}