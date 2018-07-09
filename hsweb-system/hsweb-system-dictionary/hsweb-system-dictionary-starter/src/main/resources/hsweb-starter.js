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
    website: "https://github.com/hs-web/hsweb-framework/tree/master/hsweb-system/hsweb-system-dictionary",
    author: "zh.sqy@qq.com",
    comment: "数据字典"
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
    database.createOrAlter("s_dictionary")
        .addColumn().name("u_id").alias("id").comment("ID").jdbcType(java.sql.JDBCType.VARCHAR).length(32).primaryKey().commit()
        .addColumn().name("name").alias("name").comment("字典名称").jdbcType(java.sql.JDBCType.VARCHAR).length(64).commit()
        .addColumn().name("classified_id").alias("classifiedId").comment("分类id").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("describe").alias("describe").comment("说明").jdbcType(java.sql.JDBCType.VARCHAR).length(256).commit()
        .addColumn().name("create_time").alias("createTime").comment("创建时间").jdbcType(java.sql.JDBCType.DECIMAL).length(32, 0).commit()
        .addColumn().name("creator_id").alias("creatorId").comment("创建人id").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("status").alias("status").comment("状态").jdbcType(java.sql.JDBCType.DECIMAL).length(4, 0).commit()
        .comment("数据字典").commit();

    database.createOrAlter("s_dict_item")
        .addColumn().name("u_id").alias("id").comment("ID").jdbcType(java.sql.JDBCType.VARCHAR).length(32).primaryKey().commit()
        .addColumn().name("dict_id").alias("dictId").comment("字典id").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("name").alias("name").comment("名称").jdbcType(java.sql.JDBCType.VARCHAR).length(64).commit()
        .addColumn().name("value").alias("value").comment("字典值").jdbcType(java.sql.JDBCType.VARCHAR).length(64).commit()
        .addColumn().name("text").alias("text").comment("字典文本").jdbcType(java.sql.JDBCType.VARCHAR).length(128).commit()
        .addColumn().name("value_type").alias("valueType").comment("字典值类型").jdbcType(java.sql.JDBCType.VARCHAR).length(64).commit()
        .addColumn().name("status").alias("status").comment("状态").jdbcType(java.sql.JDBCType.DECIMAL).length(4, 0).commit()
        .addColumn().name("describe").alias("describe").comment("说明").jdbcType(java.sql.JDBCType.VARCHAR).length(128).commit()
        .addColumn().name("parent_id").alias("parentId").comment("父级选项").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("path").alias("path").comment("树编码").jdbcType(java.sql.JDBCType.VARCHAR).length(3000).commit()
        .addColumn().name("search_code").alias("searchCode").comment("快速搜索码").jdbcType(java.sql.JDBCType.VARCHAR).length(128).commit()
        .addColumn().name("sort_index").alias("sortIndex").comment("排序索引").jdbcType(java.sql.JDBCType.DECIMAL).length(32, 0).commit()
        .addColumn().name("level_").alias("level").comment("树结构层级").jdbcType(java.sql.JDBCType.DECIMAL).length(32, 0).commit()
        .addColumn().name("ordinal").alias("ordinal").comment("识别码").jdbcType(java.sql.JDBCType.DECIMAL).length(32, 0).commit()
        .addColumn().name("properties").alias("properties").comment("其他自定义属性").jdbcType(java.sql.JDBCType.CLOB).commit()
        .comment("数据字典解析配置").commit();

    database.createOrAlter("s_dict_parser")
        .addColumn().name("u_id").alias("id").comment("ID").jdbcType(java.sql.JDBCType.VARCHAR).length(32).primaryKey().commit()
        .addColumn().name("v_t_parser").alias("valueToTextParser").comment("值到文本转换方式").jdbcType(java.sql.JDBCType.CLOB).commit()
        .addColumn().name("t_v_parser").alias("textToValueParser").comment("文本到值转换方式").jdbcType(java.sql.JDBCType.CLOB).commit()
        .addColumn().name("on_error").alias("onError").comment("转换失败时的操作").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("create_time").alias("createTime").comment("创建时间").jdbcType(java.sql.JDBCType.DECIMAL).length(32, 0).commit()
        .addColumn().name("creator_id").alias("creatorId").comment("创建人id").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("update_time").alias("updateTime").comment("更新时间").jdbcType(java.sql.JDBCType.DECIMAL).length(32, 0).commit()
        .addColumn().name("name").alias("name").comment("名称").jdbcType(java.sql.JDBCType.VARCHAR).length(128).commit()
        .addColumn().name("describe").alias("describe").comment("说明").jdbcType(java.sql.JDBCType.VARCHAR).length(256).commit()
        .addColumn().name("classified_id").alias("classifiedId").comment("分类id").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .comment("数据字典解析配置").commit();

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