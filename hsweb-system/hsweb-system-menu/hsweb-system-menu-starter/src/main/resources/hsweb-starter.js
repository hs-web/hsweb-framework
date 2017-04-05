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
    website: "https://github.com/hs-web/hsweb-framework/tree/master/hsweb-system/hsweb-system-menu",
    author: "zh.sqy@qq.com",
    comment: "菜单分组"
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
        .addColumn().name("enabled").varchar(32).comment("是否启用").commit()
        .comment("系统菜单表").commit()

    database.createOrAlter("s_menu_group")
        .addColumn().name("u_id").alias("id").comment("ID").jdbcType(java.sql.JDBCType.VARCHAR).length(32).primaryKey().commit()
        .addColumn().name("name").alias("name").comment("分组名称").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("describe").alias("describe").comment("分组描述").jdbcType(java.sql.JDBCType.VARCHAR).length(64).commit()
        .addColumn().name("default_group").alias("defaultGroup").comment("是否默认").jdbcType(java.sql.JDBCType.DECIMAL).length(4, 0).commit()
        .addColumn().name("tree_code").alias("treeCode").comment("树结构编码").jdbcType(java.sql.JDBCType.VARCHAR).length(4000).commit()
        .addColumn().name("parent_id").alias("parentId").comment("父级id").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("level").alias("level").comment("树层级").jdbcType(java.sql.JDBCType.DECIMAL).length(32, 0).commit()
        .addColumn().name("sort_index").alias("sortIndex").comment("排序序号").jdbcType(java.sql.JDBCType.DECIMAL).length(32, 0).commit()
        .addColumn().name("enable").alias("enable").comment("是否启用").jdbcType(java.sql.JDBCType.DECIMAL).length(4, 0).commit()
        .comment("菜单分组").commit();

    database.createOrAlter("s_menu_group_bind")
        .addColumn().name("u_id").alias("id").comment("ID").jdbcType(java.sql.JDBCType.VARCHAR).length(32).primaryKey().commit()
        .addColumn().name("menu_id").alias("menuId").comment("菜单id").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("group_id").alias("groupId").comment("分组id").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("tree_code").alias("treeCode").comment("树结构编码").jdbcType(java.sql.JDBCType.VARCHAR).length(4000).commit()
        .addColumn().name("parent_id").alias("parentId").comment("父级id").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("level").alias("level").comment("树层级").jdbcType(java.sql.JDBCType.DECIMAL).length(32, 0).commit()
        .addColumn().name("sort_index").alias("sortIndex").comment("排序序号").jdbcType(java.sql.JDBCType.DECIMAL).length(32, 0).commit()
        .addColumn().name("enable").alias("enable").comment("是否启用").jdbcType(java.sql.JDBCType.DECIMAL).length(4, 0).commit()
        .addColumn().name("actions").alias("actions").comment("可选按钮").jdbcType(java.sql.JDBCType.VARCHAR).length(4000).commit()
        .addColumn().name("data_accesses").alias("dataAccesses").comment("行级权限控制配置").jdbcType(java.sql.JDBCType.CLOB).commit()
        .addColumn().name("field_accesses").alias("fieldAccesses").comment("列级权限控制").jdbcType(java.sql.JDBCType.CLOB).commit()
        .comment("菜单分组关联").commit();
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