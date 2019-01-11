//组件信息
var info = {
    groupId: "${project.groupId}",
    artifactId: "${project.artifactId}",
    version: "${project.version}",
    website: "https://github.com/hs-web/hsweb-framework",
    author: "admin@hsweb.me",
    comment: "动态表单"
};

//版本更新信息
var versions = [
    // {
    //     version: "3.0.2",
    //     upgrade: function (context) {
    //         java.lang.System.out.println("更新到3.0.2了");
    //     }
    // }
];
var JDBCType = java.sql.JDBCType;
function install(context) {
    var database = context.database;
    database.createOrAlter("s_dyn_form")
        .addColumn().name("u_id").alias("id").comment("ID").jdbcType(java.sql.JDBCType.VARCHAR).length(32).primaryKey().commit()
        .addColumn().name("name").alias("name").comment("表单名称").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("t_name").alias("tableName").comment("数据库表名").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("describe").alias("describe").comment("备注").jdbcType(java.sql.JDBCType.VARCHAR).length(512).commit()
        .addColumn().name("type").alias("type").comment("表单类型").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("version").alias("version").comment("版本").jdbcType(java.sql.JDBCType.DECIMAL).length(32, 0).commit()
        .addColumn().name("is_deployed").alias("isDeployed").comment("是否已发布").jdbcType(java.sql.JDBCType.DECIMAL).length(1, 0).commit()
        .addColumn().name("alias").alias("alias").comment("别名").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("triggers").alias("triggers").comment("触发器").jdbcType(java.sql.JDBCType.CLOB).commit()
        .addColumn().name("correlations").alias("correlations").comment("表链接").jdbcType(java.sql.JDBCType.CLOB).commit()
        .addColumn().name("data_source_id").alias("dataSourceId").comment("数据源id,为空使用默认数据源").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("creator_id").alias("creatorId").comment("创建人id").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("create_time").alias("createTime").comment("创建时间").jdbcType(java.sql.JDBCType.DECIMAL).length(32, 0).commit()
        .addColumn().name("update_time").alias("updateTime").comment("修改时间").jdbcType(java.sql.JDBCType.DECIMAL).length(32, 0).commit()
        .addColumn().name("properties").alias("properties").comment("其他配置").jdbcType(java.sql.JDBCType.CLOB).commit()
        .addColumn().name("tags").alias("tags").comment("标签").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .comment("动态表单").commit();

    database.createOrAlter("s_dyn_form_column")
        .addColumn().name("u_id").alias("id").comment("ID").jdbcType(java.sql.JDBCType.VARCHAR).length(32).primaryKey().commit()
        .addColumn().name("form_id").alias("formId").comment("表单ID").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("name").alias("name").comment("字段名称").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("column_name").alias("columnName").comment("数据库列").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("describe").alias("describe").comment("备注").jdbcType(java.sql.JDBCType.VARCHAR).length(512).commit()
        .addColumn().name("alias").alias("alias").comment("别名").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("java_type").alias("javaType").comment("java类型").jdbcType(java.sql.JDBCType.VARCHAR).length(128).commit()
        .addColumn().name("jdbc_type").alias("jdbcType").comment("jdbc类型").jdbcType(java.sql.JDBCType.VARCHAR).length(128).commit()
        .addColumn().name("data_type").alias("dataType").comment("数据类型").jdbcType(java.sql.JDBCType.VARCHAR).length(128).commit()
        .addColumn().name("length").alias("length").comment("长度").jdbcType(java.sql.JDBCType.DECIMAL).length(4, 0).commit()
        .addColumn().name("precision").alias("precision").comment("精度").jdbcType(java.sql.JDBCType.DECIMAL).length(4, 0).commit()
        .addColumn().name("scale").alias("scale").comment("小数点位数").jdbcType(java.sql.JDBCType.DECIMAL).length(4, 0).commit()
        .addColumn().name("properties").alias("properties").comment("其他配置").jdbcType(java.sql.JDBCType.CLOB).commit()
        .addColumn().name("dict_config").alias("dictConfig").comment("字典配置").jdbcType(java.sql.JDBCType.CLOB).commit()
        .addColumn().name("sort_index").alias("sortIndex").comment("排序序号").jdbcType(java.sql.JDBCType.DECIMAL).length(32, 0).commit()
        .addColumn().name("validator").alias("validator").comment("验证器配置").jdbcType(java.sql.JDBCType.CLOB).commit()
        .index().name("idx_dynf_form_id").column("form_id").commit()

        .comment("动态表单列").commit();

    database.createOrAlter("s_dyn_form_log")
        .addColumn().name("u_id").alias("id").comment("ID").jdbcType(java.sql.JDBCType.VARCHAR).length(32).primaryKey().commit()
        .addColumn().name("form_id").alias("formId").comment("表单ID").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("version").alias("version").comment("发布的版本").jdbcType(java.sql.JDBCType.NUMERIC).length(32, 0).commit()
        .addColumn().name("deploy_time").alias("deployTime").comment("发布时间").jdbcType(java.sql.JDBCType.DECIMAL).length(32, 0).commit()
        .addColumn().name("meta_data").alias("metaData").comment("部署的元数据").jdbcType(java.sql.JDBCType.CLOB).commit()
        .addColumn().name("status").alias("status").comment("状态").jdbcType(java.sql.JDBCType.NUMERIC).length(4, 0).commit()
        .index().name("idx_dynfl_form_id").column("form_id").commit()
        .index().name("idx_dynfl_form_id_ver").column("form_id").column("version").commit()
        .comment("表单发布日志").commit();
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