/*
 * Copyright 2016 http://www.hswebframework.org
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
    website: "http://github.com/hs-web/hsweb-framework",
    author: "zh.sqy@qq.com",
    comment: "权限管理"
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
    database.createOrAlter("s_user")
        .addColumn().name("u_id").varchar(32).notNull().primaryKey().comment("uid").commit()
        .addColumn().name("name").varchar(128).notNull().comment("姓名").commit()
        .addColumn().name("username").varchar(128).notNull().comment("用户名").commit()
        .addColumn().name("password").varchar(128).notNull().comment("密码").commit()
        .addColumn().name("salt").varchar(128).notNull().comment("密码盐").commit()
        .addColumn().name("enabled").number(1).notNull().comment("是否启用").commit()
        .addColumn().name("last_login_ip").varchar(128).comment("上一次登录的ip地址").commit()
        .addColumn().name("last_login_time").number(32).comment("上一次登录时间").commit()
        .addColumn().name("creator_id").varchar(32).notNull().comment("创建者ID").commit()
        .addColumn().name("create_time").number(32).notNull().comment("创建时间").commit()
        .comment("用户表").commit();

    database.createOrAlter("s_role")
        .addColumn().name("u_id").varchar(32).notNull().primaryKey().comment("uid").commit()
        .addColumn().name("name").varchar(128).notNull().comment("角色名称").commit()
        .addColumn().name("describe").varchar(128).comment("说明").commit()
        .addColumn().name("enabled").number(1).notNull().comment("是否启用").commit()
        .comment("角色表").commit();

    database.createOrAlter("s_permission")
        .addColumn().name("u_id").varchar(32).notNull().primaryKey().comment("uid").commit()
        .addColumn().name("name").varchar(128).notNull().comment("角色名称").commit()
        .addColumn().name("describe").varchar(128).comment("说明").commit()
        .addColumn().name("status").number(4).notNull().comment("状态").commit()
        .addColumn().name("actions").clob().notNull().comment("可选操作(按钮)").commit()
        .addColumn().name("data_access").clob().notNull().comment("数据级控制配置").commit()
        .addColumn().name("optional_fields").clob().notNull().comment("可选字段").commit()
        .comment("权限表").commit();

    database.createOrAlter("s_permission_role")
        .addColumn().name("role_id").varchar(32).notNull().comment("角色ID").commit()
        .addColumn().name("permission_id").varchar(32).notNull().comment("权限ID").commit()
        .addColumn().name("actions").clob().notNull().comment("可选操作").commit()
        .addColumn().name("data_access").clob().notNull().comment("数据级控制配置").commit()
        .comment("权限与角色关联表").commit();

    database.createOrAlter("s_user_role")
        .addColumn().name("role_id").varchar(32).notNull().comment("角色ID").commit()
        .addColumn().name("user_id").varchar(32).notNull().comment("用户ID").commit()
        .comment("用户与角色关联表").commit();

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