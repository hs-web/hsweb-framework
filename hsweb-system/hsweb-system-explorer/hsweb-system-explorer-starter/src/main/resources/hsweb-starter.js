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
    comment: "资源管理器"
};

//版本更新信息
var versions = [
    // {
    //     version: "3.0.0",
    //     upgrade: function (context) {
    //     }
    // }
];
var JDBCType = java.sql.JDBCType;
function install(context) {
    var database = context.database;
    database.createOrAlter("s_menu")
        .addColumn().name("u_id").varchar(32).notNull().primaryKey().comment("uid").commit()
        .addColumn().name("name").varchar(64).notNull().comment("名称").commit()
        .addColumn().name("parent_id").varchar(32).comment("父级ID").commit()
        .addColumn().name("permission_id").varchar(32).comment("权限ID").commit()
        .addColumn().name("tree_code").varchar(2048).notNull().comment("树编码").commit()
        .addColumn().name("sort_index").number(32).notNull().comment("树编码").commit()
        .addColumn().name("describe").varchar(128).comment("备注").commit()
        .addColumn().name("url").varchar(2000).comment("URL").commit()
        .addColumn().name("icon").varchar(512).comment("图标").commit()
        .addColumn().name("authentication").varchar(128).comment("认证方式").commit()
        .addColumn().name("authentication_config").clob().comment("认证配置").commit()
        .addColumn().name("on_init").clob().comment("加载后执行脚本").commit()
        .addColumn().name("enabled").varchar(32).comment("是否有效").commit()
        .addColumn().name("actions").clob().comment("可选操作集合").commit()
        .comment("系统菜单表").commit()
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