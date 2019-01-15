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
    website: "http://github.com/hs-web/hsweb-framework",
    author: "zh.sqy@qq.com",
    comment: "权限管理"
};

//版本更新信息
var versions = [
    {
        version: "3.0.4",
        upgrade: function (context) {
            var database = context.database;
            database.createOrAlter("s_user_setting")
                .addColumn().name("permission").varchar(32).comment("用户可操作权限").commit()
                .commit();
        }
    }
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
        .addColumn().name("status").number(4).notNull().comment("用户状态").commit()
        .addColumn().name("last_login_ip").varchar(128).comment("上一次登录的ip地址").commit()
        .addColumn().name("last_login_time").number(32).comment("上一次登录时间").commit()
        .addColumn().name("creator_id").varchar(32).comment("创建者ID").commit()
        .addColumn().name("create_time").number(32).notNull().comment("创建时间").commit()
        //用户名唯一索引
        .index().name("idx_user_username").column("username").unique().commit()
        .comment("用户表").commit();

    database.createOrAlter("s_role")
        .addColumn().name("u_id").varchar(32).notNull().primaryKey().comment("uid").commit()
        .addColumn().name("name").varchar(128).notNull().comment("角色名称").commit()
        .addColumn().name("describe").varchar(128).comment("说明").commit()
        .addColumn().name("status").number(4).notNull().comment("状态").commit()
        .comment("角色表").commit();

    database.createOrAlter("s_permission")
        .addColumn().name("u_id").varchar(32).notNull().primaryKey().comment("uid").commit()
        .addColumn().name("name").varchar(128).notNull().comment("角色名称").commit()
        .addColumn().name("describe").varchar(128).comment("说明").commit()
        .addColumn().name("status").number(4).notNull().comment("状态").commit()
        .addColumn().name("actions").clob().notNull().comment("可选操作(按钮)").commit()
        .addColumn().name("spt_da_types").clob().comment("支持的数据权限类型").commit()
        .addColumn().name("optional_fields").clob().comment("可选字段").commit()
        .addColumn().name("parents").clob().comment("关联其他权限").commit()
        .addColumn().name("type").varchar(128).comment("类型").commit()
        .comment("权限表").commit();

    database.createOrAlter("s_permission_role")
        .addColumn().name("role_id").varchar(32).notNull().comment("角色ID").commit()
        .addColumn().name("permission_id").varchar(32).notNull().comment("权限ID").commit()
        .addColumn().name("actions").clob().comment("可选操作").commit()
        .addColumn().name("data_access").clob().comment("数据级控制配置").commit()
        .comment("权限与角色关联表").commit();

    database.createOrAlter("s_user_role")
        .addColumn().name("role_id").varchar(32).notNull().comment("角色ID").commit()
        .addColumn().name("user_id").varchar(32).notNull().comment("用户ID").commit()
        .index().name("idx_ur_user_id").column("user_id").commit()
        .index().name("idx_ur_role_id").column("role_id").commit()

        .comment("用户与角色关联表").commit();

    //权限设置
    database.createOrAlter("s_autz_setting")
        .addColumn().name("u_id").varchar(32).notNull().primaryKey().comment("uid").commit()
        .addColumn().name("type").varchar(32).notNull().comment("权限类型").commit()
        .addColumn().name("setting_for").varchar(64).notNull().comment("设置给谁").commit()
        .addColumn().name("describe").varchar(256).comment("备注").commit()
        .addColumn().name("status").number(4, 0).comment("设置给谁").commit()

        .index().name("idx_as_type_setting_for").column("type").column("setting_for").commit()
        .comment("权限设置表").commit();

    database.createOrAlter("s_autz_detail")
        .addColumn().name("u_id").varchar(32).notNull().primaryKey().comment("uid").commit()
        .addColumn().name("permission_id").varchar(32).notNull().comment("权限ID").commit()
        .addColumn().name("setting_id").varchar(64).notNull().comment("设置ID").commit()
        .addColumn().name("actions").clob().comment("可操作类型").commit()
        .addColumn().name("data_accesses").clob().comment("数据权限控制").commit()
        .addColumn().name("status").number(4, 0).comment("状态").commit()
        .addColumn().name("priority").number(32, 0).comment("优先级").commit()
        .addColumn().name("is_merge").number(4, 0).comment("是否合并").commit()
        .index().name("idx_ad_setting_id").column("setting_id").commit()

        .comment("权限设置详情表").commit();

    database.createOrAlter("s_autz_menu")
        .addColumn().name("u_id").varchar(32).notNull().primaryKey().comment("uid").commit()
        .addColumn().name("parent_id").varchar(32).comment("父级ID").commit()
        .addColumn().name("menu_id").varchar(32).notNull().comment("菜单ID").commit()
        .addColumn().name("setting_id").varchar(64).notNull().comment("设置ID").commit()
        .addColumn().name("path").varchar(2048).notNull().comment("树编码").commit()
        .addColumn().name("sort_index").number(32).comment("树编码").commit()
        .addColumn().name("status").number(4, 0).comment("状态").commit()
        .addColumn().name("level").number(32, 0).comment("树深度").commit()
        .addColumn().name("config").clob().comment("其他配置").commit()
        .index().name("idx_ame_setting_id").column("setting_id").commit()
        .index().name("idx_ame_parent_id").column("parent_id").commit()
        .index().name("idx_ame_path").column("path").commit()
        .comment("权限设置菜单表").commit();

    // 菜单
    database.createOrAlter("s_menu")
        .addColumn().name("u_id").varchar(32).notNull().primaryKey().comment("uid").commit()
        .addColumn().name("name").varchar(64).notNull().comment("名称").commit()
        .addColumn().name("parent_id").varchar(32).comment("父级ID").commit()
        .addColumn().name("permission_id").varchar(2048).comment("权限ID").commit()
        .addColumn().name("path").varchar(2048).notNull().comment("树编码").commit()
        .addColumn().name("sort_index").number(32).comment("树编码").commit()
        .addColumn().name("describe").varchar(128).comment("备注").commit()
        .addColumn().name("url").varchar(2000).comment("URL").commit()
        .addColumn().name("icon").varchar(512).comment("图标").commit()
        .addColumn().name("status").alias("status").comment("状态").jdbcType(java.sql.JDBCType.DECIMAL).length(4, 0).commit()
        .index().name("idx_menu_path").column("path").commit()
        .index().name("idx_menu_parent_id").column("parent_id").commit()

        .comment("系统菜单表").commit()

    database.createOrAlter("s_menu_group")
        .addColumn().name("u_id").alias("id").comment("ID").jdbcType(java.sql.JDBCType.VARCHAR).length(32).primaryKey().commit()
        .addColumn().name("parent_id").varchar(32).comment("父级ID").commit()
        .addColumn().name("name").alias("name").comment("分组名称").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("describe").alias("describe").comment("分组描述").jdbcType(java.sql.JDBCType.VARCHAR).length(64).commit()
        .addColumn().name("default_group").alias("defaultGroup").comment("是否默认").jdbcType(java.sql.JDBCType.DECIMAL).length(4, 0).commit()
        .addColumn().name("path").alias("path").comment("树路径").jdbcType(java.sql.JDBCType.VARCHAR).length(4000).commit()
        .addColumn().name("parent_id").alias("parentId").comment("父级id").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("level").alias("level").comment("树层级").jdbcType(java.sql.JDBCType.DECIMAL).length(32, 0).commit()
        .addColumn().name("sort_index").alias("sortIndex").comment("排序序号").jdbcType(java.sql.JDBCType.DECIMAL).length(32, 0).commit()
        .addColumn().name("status").alias("status").comment("状态").jdbcType(java.sql.JDBCType.DECIMAL).length(4, 0).commit()
        .comment("菜单分组").commit();

    database.createOrAlter("s_menu_group_bind")
        .addColumn().name("u_id").alias("id").comment("ID").jdbcType(java.sql.JDBCType.VARCHAR).length(32).primaryKey().commit()
        .addColumn().name("menu_id").alias("menuId").comment("菜单id").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("group_id").alias("groupId").comment("分组id").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("path").alias("path").comment("树结构编码").jdbcType(java.sql.JDBCType.VARCHAR).length(4000).commit()
        .addColumn().name("parent_id").alias("parentId").comment("父级id").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("level").alias("level").comment("树层级").jdbcType(java.sql.JDBCType.DECIMAL).length(32, 0).commit()
        .addColumn().name("sort_index").alias("sortIndex").comment("排序序号").jdbcType(java.sql.JDBCType.DECIMAL).length(32, 0).commit()
        .addColumn().name("status").alias("status").comment("状态").jdbcType(java.sql.JDBCType.DECIMAL).length(4, 0).commit()
        .comment("菜单分组关联").commit();

    database.createOrAlter("s_user_setting")
        .addColumn().name("u_id").varchar(32).notNull().primaryKey().comment("uid").commit()
        .addColumn().name("name").varchar(128).comment("配置名称").commit()
        .addColumn().name("describe").varchar(512).comment("说明").commit()
        .addColumn().name("user_id").varchar(32).notNull().comment("用户ID").commit()
        .addColumn().name("key").varchar(128).notNull().comment("配置标识").commit()
        .addColumn().name("setting").clob().comment("配置内容").commit()
        .addColumn().name("setting_id").varchar(32).notNull().comment("自定义配置id").commit()
        .addColumn().name("create_time").datetime().notNull().comment("创建时间").commit()
        .addColumn().name("update_time").datetime().comment("创建时间").commit()
        .index().name("idx_uset_user_id").column("user_id").commit()
        .index().name("idx_uset_user_id_key").column("user_id").column("key").commit()
        .index().name("idx_uset_user_id_key_setting").column("user_id").column("key").column("setting_id").commit()
        .comment("用户设置表").commit();
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