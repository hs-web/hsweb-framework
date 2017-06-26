/*
 *  Copyright 2016 http://www.hswebframework.org
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
//组件信息
var info = {
    groupId: "${project.groupId}",
    artifactId: "${project.artifactId}",
    version: "${project.version}",
    website: "http://github.com/hs-web/hsweb-framework",
    author: "zh.sqy@qq.com",
    comment: "OAuth2.0 server"
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
    database.createOrAlter("s_oauth2_client")
        .addColumn().name("u_id").varchar(32).notNull().primaryKey().comment("client_id").commit()
        .addColumn().name("secret").varchar(64).notNull().comment("client_secret").commit()
        .addColumn().name("name").varchar(128).notNull().comment("客户端名称").commit()
        .addColumn().name("describe").varchar(256).comment("备注").commit()
        .addColumn().name("type").varchar(128).notNull().comment("客户端类型").commit()
        .addColumn().name("owner_id").varchar(32).notNull().comment("绑定的用户ID").commit()
        .addColumn().name("creator_id").varchar(32).notNull().comment("创建者ID").commit()
        .addColumn().name("redirect_uri").varchar(1024).notNull().comment("redirect_uri").commit()
        .addColumn().name("create_time").number(32).notNull().comment("创建时间").commit()
        .addColumn().name("support_grant_types").varchar(2048).comment("支持的授权列表").commit()
        .addColumn().name("default_expires_in").number(16).comment("默认认证过期时间").commit()
        .addColumn().name("default_grant_scope").clob().comment("默认认证范围").commit()
        .addColumn().name("status").number(4).comment("状态").commit()
        .comment("OAuth2客户端").commit();

    database.createOrAlter("s_oauth2_access")
        .addColumn().name("client_id").varchar(32).notNull().comment("client_id").commit()
        .addColumn().name("owner_id").varchar(32).notNull().comment("授权对应的用户ID").commit()
        .addColumn().name("access_token").varchar(32).notNull().comment("授权码").commit()
        .addColumn().name("expires_in").varchar(32).notNull().comment("有效期").commit()
        .addColumn().name("refresh_token").varchar(32).notNull().comment("用于更新授权的token").commit()
        .addColumn().name("create_time").number(32).notNull().comment("创建时间").commit()
        .addColumn().name("update_time").number(32).comment("更新时间").commit()
        .addColumn().name("scope").clob().comment("授权范围").commit()
        .comment("OAuth2授权认证信息").commit();

    database.createOrAlter("s_oauth2_auth_code")
        .addColumn().name("client_id").varchar(32).notNull().comment("client_id").commit()
        .addColumn().name("user_id").varchar(32).notNull().comment("授权对应的用户ID").commit()
        .addColumn().name("code").varchar(32).notNull().comment("授权码").commit()
        .addColumn().name("create_time").number(32).notNull().comment("创建时间").commit()
        .addColumn().name("scope").clob().notNull().comment("授权范围").commit()
        .addColumn().name("redirect_uri").varchar(1024).notNull().comment("重定向URI").commit()
        .comment("OAuth2授权码信息").commit();
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