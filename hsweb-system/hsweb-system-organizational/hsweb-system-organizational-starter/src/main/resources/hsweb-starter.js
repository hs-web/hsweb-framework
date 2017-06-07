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
    website: "https://github.com/hs-web/hsweb-framework/tree/master/hsweb-system/hsweb-system-organizational",
    author: "admin@hsweb.me",
    comment: "组织架构"
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
    database.createOrAlter("s_organization")
        .addColumn().name("u_id").alias("id").comment("ID").jdbcType(java.sql.JDBCType.VARCHAR).length(32).primaryKey().commit()
        .addColumn().name("name").alias("name").comment("名称").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("full_name").alias("fullName").comment("全称").jdbcType(java.sql.JDBCType.VARCHAR).length(256).commit()
        .addColumn().name("code").alias("code").comment("机构编码").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("optional_roles").alias("optionalRoles").comment("可选角色").jdbcType(java.sql.JDBCType.CLOB).commit()
        .addColumn().name("parent_id").alias("parentId").comment("上级机构id").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("path").alias("path").comment("树定位码").jdbcType(java.sql.JDBCType.VARCHAR).length(1024).commit()
        .addColumn().name("sort_index").alias("sortIndex").comment("树结构编码").jdbcType(java.sql.JDBCType.DECIMAL).length(32, 0).commit()
        .addColumn().name("status").alias("status").comment("状态").jdbcType(java.sql.JDBCType.DECIMAL).length(32, 0).commit()
        .addColumn().name("level").alias("level").comment("级别").jdbcType(java.sql.JDBCType.DECIMAL).length(32, 0).commit()
        .comment("组织").commit();

    database.createOrAlter("s_department")
        .addColumn().name("u_id").alias("id").comment("ID").jdbcType(java.sql.JDBCType.VARCHAR).length(32).primaryKey().commit()
        .addColumn().name("name").alias("name").comment("名称").jdbcType(java.sql.JDBCType.VARCHAR).length(64).commit()
        .addColumn().name("org_id").alias("orgId").comment("所在组织id").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("code").alias("code").comment("部门编码").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("parent_id").alias("parentId").comment("父级id").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("path").alias("path").comment("树结构编码").jdbcType(java.sql.JDBCType.VARCHAR).length(3000).commit()
        .addColumn().name("sort_index").alias("sortIndex").comment("排序序号").jdbcType(java.sql.JDBCType.DECIMAL).length(32, 0).commit()
        .addColumn().name("status").alias("status").comment("状态").jdbcType(java.sql.JDBCType.DECIMAL).length(32, 0).commit()
        .addColumn().name("level").alias("level").comment("级别").jdbcType(java.sql.JDBCType.DECIMAL).length(32, 0).commit()
        .comment("部门").commit();

    database.createOrAlter("s_position")
        .addColumn().name("u_id").alias("id").comment("ID").jdbcType(java.sql.JDBCType.VARCHAR).length(32).primaryKey().commit()
        .addColumn().name("name").alias("name").comment("职位名称").jdbcType(java.sql.JDBCType.VARCHAR).length(64).commit()
        .addColumn().name("department_id").alias("departmentId").comment("部门id").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("roles").alias("roles").comment("持有的角色").jdbcType(java.sql.JDBCType.CLOB).commit()
        .addColumn().name("remark").alias("remark").comment("备注").jdbcType(java.sql.JDBCType.VARCHAR).length(256).commit()
        .addColumn().name("parent_id").alias("parentId").comment("父级id").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("path").alias("path").comment("树结构编码").jdbcType(java.sql.JDBCType.VARCHAR).length(4000).commit()
        .addColumn().name("sort_index").alias("sortIndex").comment("排序索引").jdbcType(java.sql.JDBCType.DECIMAL).length(32, 0).commit()
        .addColumn().name("level").alias("level").comment("级别").jdbcType(java.sql.JDBCType.DECIMAL).length(32, 0).commit()
        .comment("职位").commit();

    database.createOrAlter("s_person")
        .addColumn().name("u_id").alias("id").comment("ID").jdbcType(java.sql.JDBCType.VARCHAR).length(32).primaryKey().commit()
        .addColumn().name("name").alias("name").comment("姓名").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("sex").alias("sex").comment("性别").jdbcType(java.sql.JDBCType.DECIMAL).length(4, 0).commit()
        .addColumn().name("email").alias("email").comment("电子邮箱").jdbcType(java.sql.JDBCType.VARCHAR).length(128).commit()
        .addColumn().name("phone").alias("phone").comment("联系电话").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("photo").alias("photo").comment("照片").jdbcType(java.sql.JDBCType.VARCHAR).length(128).commit()
        .addColumn().name("user_id").alias("userId").comment("关联用户id").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("status").alias("status").comment("状态").jdbcType(java.sql.JDBCType.DECIMAL).length(32, 0).commit()
        .addColumn().name("remark").alias("remark").comment("备注").jdbcType(java.sql.JDBCType.VARCHAR).length(256).commit()
        .comment("人员").commit();

    database.createOrAlter("s_person_position")
        .addColumn().name("person_id").alias("personId").comment("人员id").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("position_id").alias("positionId").comment("职位id").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .comment("人员职位关联").commit();

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