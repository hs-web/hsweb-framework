/*
 *
 *  * Copyright 2019 http://www.hswebframework.org
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */
//组件信息
var info = {
    groupId: "org.hswebframework",
    artifactId: "hsweb-starter-test",
    version: "3.0.2",
    configClass: "",
    website: "http://github.com/hs-web",
    comment: "测试"
};

//版本更新信息
var versions = [
    {
        version: "3.0.0",
        upgrade: function (context) {
            java.lang.System.out.println("更新到3.0.2了");
        }
    },
    {
        version: "3.0.1",
        upgrade: function (context) {
            java.lang.System.out.println("更新到3.0.2了");
        }
    },
    {
        version: "3.0.2",
        upgrade: function (context) {
            java.lang.System.out.println("更新到3.0.1了");
        }
    }
];

function install(context) {
    var database = context.database;
    database.createOrAlter("s_user")
        .addColumn().name("u_id").varchar(32).notNull().primaryKey().comment("uid").commit()
        .addColumn().name("name").varchar(128).notNull().comment("姓名").commit()
        .addColumn().name("username").varchar(128).notNull().comment("用户名").commit()
        .addColumn().name("password").varchar(128).notNull().comment("密码").commit()
        .addColumn().name("salt").varchar(128).notNull().comment("密码盐").commit()
        .addColumn().name("status").number(4).notNull().comment("用户状态").commit()
        .addColumn().name("last_login_ip").varchar(128).comment("上一次登录的ip地址").commit()
        .addColumn().name("last_login_time").number(32).comment("上一次登录时间").commit()
        .addColumn().name("creator_id").varchar(32).comment("创建者ID").commit()
        .addColumn().name("create_time").number(32).notNull().comment("创建时间").commit()
        .comment("用户表").commit();

    database.createOrAlter("s_user_test")
        .addColumn().name("u_id").varchar(32).notNull().primaryKey().comment("uid").commit()
        .addColumn().name("name").varchar(128).notNull().comment("姓名").commit()
        .addColumn().name("username").varchar(128).notNull().comment("用户名").commit()
        .addColumn().name("password").varchar(128).notNull().comment("密码").commit()
        .addColumn().name("salt").varchar(128).notNull().comment("密码盐").commit()
        .addColumn().name("status").number(4).notNull().comment("用户状态").commit()
        .addColumn().name("last_login_ip").varchar(128).comment("上一次登录的ip地址").commit()
        .addColumn().name("last_login_time").number(32).comment("上一次登录时间").commit()
        .addColumn().name("creator_id").varchar(32).comment("创建者ID").commit()
        .addColumn().name("create_time").number(32).notNull().comment("创建时间").commit()
        .comment("测试用户表").commit();

    java.lang.System.out.println("安装了");
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

    }).onInitialize(function (context) {
    java.lang.System.out.println("初始化啦");
});