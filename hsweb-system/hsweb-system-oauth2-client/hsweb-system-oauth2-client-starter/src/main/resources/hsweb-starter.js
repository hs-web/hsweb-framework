/*
 * Copyright 2019 http://www.hswebframework.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
//组件信息
var info = {
    groupId: "${project.groupId}",
    artifactId: "${project.artifactId}",
    version: "${project.version}",
    website: "https://github.com/hs-web/hsweb-framework/tree/master/hsweb-system/hsweb-system-oauth2-client",
    author: "zh.sqy@qq.com",
    comment: "OAuth2服务配置"
};

//版本更新信息
var versions = [
    // {
    //     version: "3.0.0",
    //     upgrade: function (context) {
    //         java.lang.System.out.println("更新到3.0.2了");
    //     }
    // }
];
var JDBCType = java.sql.JDBCType;
function install(context) {
    var database = context.database;
    database.createOrAlter("s_oauth2_server")
        .addColumn().name("u_id").alias("id").comment("ID").jdbcType(java.sql.JDBCType.VARCHAR).length(32).primaryKey().commit()
        .addColumn().name("name").alias("name").comment("服务名称").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("describe").alias("describe").comment("备注").jdbcType(java.sql.JDBCType.VARCHAR).length(128).commit()
        .addColumn().name("api_base_url").alias("apiBaseUrl").comment("api根地址").jdbcType(java.sql.JDBCType.VARCHAR).length(512).commit()
        .addColumn().name("auth_url").alias("authUrl").comment("认证地址").jdbcType(java.sql.JDBCType.VARCHAR).length(512).commit()
        .addColumn().name("redirect_uri").alias("redirectUri").comment("重定向地址").jdbcType(java.sql.JDBCType.VARCHAR).length(512).commit()
        .addColumn().name("access_token_url").alias("accessTokenUrl").comment("token获取地址").jdbcType(java.sql.JDBCType.VARCHAR).length(512).commit()
        .addColumn().name("client_id").alias("clientId").comment("客户端id").jdbcType(java.sql.JDBCType.VARCHAR).length(128).commit()
        .addColumn().name("client_secret").alias("clientSecret").comment("客户端密钥").jdbcType(java.sql.JDBCType.VARCHAR).length(128).commit()
        .addColumn().name("provider").alias("provider").comment("服务提供商").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("properties").alias("properties").comment("其他配置").jdbcType(java.sql.JDBCType.CLOB).commit()
        .addColumn().name("status").alias("status").comment("状态").jdbcType(java.sql.JDBCType.DECIMAL).length(4, 0).commit()
        .comment("OAuth2 服务配置").commit();

    database.createOrAlter("s_oauth2_user_token")
        .addColumn().name("u_id").alias("id").comment("ID").jdbcType(java.sql.JDBCType.VARCHAR).length(32).primaryKey().commit()
        .addColumn().name("client_user_id").alias("clientUserId").comment("客户端用户id").jdbcType(java.sql.JDBCType.VARCHAR).length(128).commit()
        .addColumn().name("server_user_id").alias("serverUserId").comment("服务端用户id").jdbcType(java.sql.JDBCType.VARCHAR).length(128).commit()
        .addColumn().name("server_id").alias("serverId").comment("服务端id").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("client_id").alias("clientId").comment("客户端id").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("access_token").alias("accessToken").comment("授权码").jdbcType(java.sql.JDBCType.VARCHAR).length(128).commit()
        .addColumn().name("refresh_token").alias("refreshToken").comment("更新码").jdbcType(java.sql.JDBCType.VARCHAR).length(128).commit()
        .addColumn().name("expires_in").alias("expireIn").comment("有效期").jdbcType(java.sql.JDBCType.DECIMAL).length(32, 0).commit()
        .addColumn().name("scope").alias("scope").comment("授权范围").jdbcType(java.sql.JDBCType.CLOB).commit()
        .addColumn().name("create_time").alias("createTime").comment("创建时间").jdbcType(java.sql.JDBCType.DECIMAL).length(32, 0).commit()
        .addColumn().name("update_time").alias("updateTime").comment("更新时间").jdbcType(java.sql.JDBCType.DECIMAL).length(32, 0).commit()
        .addColumn().name("grant_type").alias("grant_type").comment("授权方式").jdbcType(java.sql.JDBCType.VARCHAR).length(128).commit()
        .index().name("idx_oauth_cli_c_user_id").column("client_user_id").commit()
        .index().name("idx_oauth_cli_s_user_id").column("server_user_id").commit()
        .index().name("idx_oauth_cli_access_token").column("access_token").commit()
        .index().name("idx_oauth_cli_refresh_token").column("refresh_token").commit()

        .comment("OAuth2用户授权信息").commit();
}

//设置依赖
dependency.setup(info)
    .onInstall(install)
    .onUpgrade(function (context) { //更新时执行
        var upgrader = context.upgrader;
        upgrader.filter(versions)
            .upgrade(function (newVer) {
                newVer.upgrade(context);
            });
    })
    .onUninstall(function (context) { //卸载时执行

    });