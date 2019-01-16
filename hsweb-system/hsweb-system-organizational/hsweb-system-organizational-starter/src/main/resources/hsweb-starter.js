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
    database.createOrAlter("s_district")
        .addColumn().name("u_id").alias("id").comment("ID").jdbcType(java.sql.JDBCType.VARCHAR).length(32).primaryKey().commit()
        .addColumn().name("name").alias("name").comment("区域名称,如重庆市").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("full_name").alias("fullName").comment("区域全程,如重庆市江津区").jdbcType(java.sql.JDBCType.VARCHAR).length(512).commit()
        .addColumn().name("level_name").alias("levelName").comment("区域级别名称,如:省").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("level_code").alias("levelCode").comment("区域级别编码,如:province").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("code").alias("code").comment("行政区域代码,如:500000").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("parent_id").alias("parentId").comment("父级行政区域").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("path").alias("path").comment("树路径,如: asb3-lsat").jdbcType(java.sql.JDBCType.VARCHAR).length(3000).commit()
        .addColumn().name("describe").alias("describe").comment("说明").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("status").alias("status").comment("状态").jdbcType(java.sql.JDBCType.DECIMAL).length(4, 0).commit()
        .addColumn().name("sort_index").alias("sortIndex").comment("排序索引").jdbcType(java.sql.JDBCType.DECIMAL).length(32, 0).commit()
        .index().name("idx_district_parent_id").column("parent_id").commit()
        .index().name("idx_district_path").column("path").commit()

        .comment("行政区域").commit();

    database.createOrAlter("s_organization")
        .addColumn().name("u_id").alias("id").comment("ID").jdbcType(java.sql.JDBCType.VARCHAR).length(32).primaryKey().commit()
        .addColumn().name("name").alias("name").comment("名称").jdbcType(java.sql.JDBCType.VARCHAR).length(256).commit()
        .addColumn().name("full_name").alias("fullName").comment("全称").jdbcType(java.sql.JDBCType.VARCHAR).length(256).commit()
        .addColumn().name("code").alias("code").comment("机构编码").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("district_id").alias("districtId").comment("所在行政区域ID").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("optional_roles").alias("optionalRoles").comment("可选角色").jdbcType(java.sql.JDBCType.CLOB).commit()
        .addColumn().name("parent_id").alias("parentId").comment("上级机构id").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("path").alias("path").comment("树定位码").jdbcType(java.sql.JDBCType.VARCHAR).length(1024).commit()
        .addColumn().name("sort_index").alias("sortIndex").comment("树结构编码").jdbcType(java.sql.JDBCType.DECIMAL).length(32, 0).commit()
        .addColumn().name("status").alias("status").comment("状态").jdbcType(java.sql.JDBCType.DECIMAL).length(4, 0).commit()
        .addColumn().name("level").alias("level").comment("级别").jdbcType(java.sql.JDBCType.DECIMAL).length(32, 0).commit()
        .index().name("idx_org_parent_id").column("parent_id").commit()
        .index().name("idx_org_path").column("path").commit()
        .index().name("idx_org_district_id").column("district_id").commit()

        .comment("组织,公司").commit();

    database.createOrAlter("s_department")
        .addColumn().name("u_id").alias("id").comment("ID").jdbcType(java.sql.JDBCType.VARCHAR).length(32).primaryKey().commit()
        .addColumn().name("name").alias("name").comment("名称").jdbcType(java.sql.JDBCType.VARCHAR).length(64).commit()
        .addColumn().name("org_id").alias("orgId").comment("所在组织id").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("code").alias("code").comment("部门编码").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("parent_id").alias("parentId").comment("父级id").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("path").alias("path").comment("树结构编码").jdbcType(java.sql.JDBCType.VARCHAR).length(3000).commit()
        .addColumn().name("sort_index").alias("sortIndex").comment("排序序号").jdbcType(java.sql.JDBCType.DECIMAL).length(32, 0).commit()
        .addColumn().name("status").alias("status").comment("状态").jdbcType(java.sql.JDBCType.DECIMAL).length(4, 0).commit()
        .addColumn().name("level").alias("level").comment("级别").jdbcType(java.sql.JDBCType.DECIMAL).length(32, 0).commit()
        .index().name("idx_dept_parent_id").column("parent_id").commit()
        .index().name("idx_dept_path").column("path").commit()
        .index().name("idx_dept_org_id").column("org_id").commit()

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
        .index().name("idx_position_parent_id").column("parent_id").commit()
        .index().name("idx_position_path").column("path").commit()
        .index().name("idx_position_dept_id").column("department_id").commit()

        .comment("职位").commit();

    database.createOrAlter("s_person")
        .addColumn().name("u_id").alias("id").comment("ID").jdbcType(java.sql.JDBCType.VARCHAR).length(32).primaryKey().commit()
        .addColumn().name("name").alias("name").comment("姓名").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("sex").alias("sex").comment("性别").jdbcType(java.sql.JDBCType.DECIMAL).length(4, 0).commit()
        .addColumn().name("email").alias("email").comment("电子邮箱").jdbcType(java.sql.JDBCType.VARCHAR).length(128).commit()
        .addColumn().name("phone").alias("phone").comment("联系电话").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("photo").alias("photo").comment("照片").jdbcType(java.sql.JDBCType.VARCHAR).length(128).commit()
        .addColumn().name("user_id").alias("userId").comment("关联用户id").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("status").alias("status").comment("状态").jdbcType(java.sql.JDBCType.DECIMAL).length(4, 0).commit()
        .addColumn().name("remark").alias("remark").comment("备注").jdbcType(java.sql.JDBCType.VARCHAR).length(256).commit()
        .index().name("idx_person_user_id").column("user_id").commit()

        .comment("人员").commit();

    database.createOrAlter("s_person_position")
        .addColumn().name("person_id").alias("personId").comment("人员id").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("position_id").alias("positionId").comment("职位id").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .index().name("idx_person_pos_person_id").column("person_id").commit()
        .index().name("idx_person_pos_position_id").column("position_id").commit()

        .comment("人员职位关联").commit();

    database.createOrAlter("s_relation_def")
        .addColumn().name("u_id").alias("id").comment("ID").jdbcType(java.sql.JDBCType.VARCHAR).length(32).primaryKey().commit()
        .addColumn().name("name").alias("name").comment("关系名称").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("type_id").alias("typeId").comment("关系类型").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("status").alias("status").comment("状态").jdbcType(java.sql.JDBCType.NUMERIC).length(4,0).commit()
        .index().name("idx_relation_def_type").column("type_id").commit()

        .comment("关系定义").commit();

    database.createOrAlter("s_relation_info")
        .addColumn().name("u_id").alias("id").comment("ID").jdbcType(java.sql.JDBCType.VARCHAR).length(32).primaryKey().commit()
        .addColumn().name("relation_id").alias("relationId").comment("关系定义id").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("relation_from").alias("relationFrom").comment("关系从").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("relation_to").alias("relationTo").comment("关系至").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("relation_type_from").alias("relationTypeFrom").comment("关系类型从,如:人员").jdbcType(java.sql.JDBCType.VARCHAR).length(64).commit()
        .addColumn().name("relation_type_to").alias("relationTypeTo").comment("关系类型至,如:部门").jdbcType(java.sql.JDBCType.VARCHAR).length(64).commit()
        .addColumn().name("status").alias("status").comment("状态").jdbcType(java.sql.JDBCType.NUMERIC).length(4,0).commit()
        .index().name("idx_relation_r_id").column("relation_id").commit()
        .index().name("idx_relation_rt_from").column("relation_type_from").commit()
        .index().name("idx_relation_rt_to").column("relation_type_to").commit()
        .index().name("idx_relation_r_to").column("relation_to").commit()
        .index().name("idx_relation_r_from").column("relation_from").commit()

        .comment("关系信息").commit();

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