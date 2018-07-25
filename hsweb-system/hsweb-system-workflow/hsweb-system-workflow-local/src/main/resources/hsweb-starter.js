//组件信息
var info = {
    groupId: "${project.groupId}",
    artifactId: "${project.artifactId}",
    version: "${project.version}",
    website: "https://github.com/hs-web/hsweb-framework",
    author: "admin@hsweb.me",
    comment: ""
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
    database.createOrAlter("s_wf_proc_conf")
        .addColumn().name("u_id").alias("id").comment("ID").jdbcType(java.sql.JDBCType.VARCHAR).length(32).primaryKey().commit()
        .addColumn().name("proc_def_key").alias("processDefineKey").comment("模板定义KEY").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("proc_def_id").alias("processDefineId").comment("模板定义ID").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("form_id").alias("formId").comment("表单ID").length(32).jdbcType(java.sql.JDBCType.VARCHAR).commit()
        .addColumn().name("template_id").alias("formTemplateId").length(32).comment("前端模板配置").jdbcType(java.sql.JDBCType.VARCHAR).commit()
        .addColumn().name("permission_dimension").alias("permissionDimension").comment("启动权限配置").jdbcType(java.sql.JDBCType.CLOB).length(32).commit()
        .addColumn().name("create_time").alias("createTime").comment("创建时间").datetime().commit()
        .addColumn().name("update_time").alias("updateTime").comment("修改时间").datetime().commit()
        .addColumn().name("status").alias("status").comment("状态").jdbcType(java.sql.JDBCType.TINYINT).commit()
        .comment("工作流流程自定义配置")
        .commit();

    database.createOrAlter("s_wf_act_conf")
        .addColumn().name("u_id").alias("id").comment("ID").jdbcType(java.sql.JDBCType.VARCHAR).length(32).primaryKey().commit()
        .addColumn().name("proc_def_key").alias("processDefineKey").comment("模板定义KEY").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("proc_def_id").alias("processDefineId").comment("模板定义ID").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("act_id").alias("activityId").comment("元图ID").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("form_id").alias("formId").comment("表单ID").length(32).jdbcType(java.sql.JDBCType.VARCHAR).commit()
        .addColumn().name("template_id").alias("formTemplateId").length(32).comment("前端模板配置").jdbcType(java.sql.JDBCType.VARCHAR).commit()
        .addColumn().name("candidate_dimension").alias("candidateDimension").comment("候选人维度").jdbcType(java.sql.JDBCType.CLOB).length(32).commit()
        .addColumn().name("create_time").alias("createTime").comment("创建时间").datetime().commit()
        .addColumn().name("update_time").alias("updateTime").comment("修改时间").datetime().commit()
        .addColumn().name("status").alias("status").comment("状态").jdbcType(java.sql.JDBCType.TINYINT).commit()
        .comment("工作流环节自定义配置")
        .commit();

    database.createOrAlter("s_wf_proc_his")
        .addColumn().name("u_id").alias("id").comment("ID").jdbcType(java.sql.JDBCType.VARCHAR).length(32).primaryKey().commit()
        .addColumn().name("proc_def_key").alias("processDefineKey").comment("模板定义KEY").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("proc_def_id").alias("processDefineId").comment("模板定义ID").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("act_id").alias("activityId").comment("元图ID").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("form_id").alias("formId").comment("表单ID").length(32).jdbcType(java.sql.JDBCType.VARCHAR).commit()
        .addColumn().name("template_id").alias("formTemplateId").length(32).comment("前端模板配置").jdbcType(java.sql.JDBCType.VARCHAR).commit()
        .addColumn().name("candidate_dimension").alias("candidateDimension").comment("候选人维度").jdbcType(java.sql.JDBCType.CLOB).length(32).commit()
        .addColumn().name("create_time").alias("createTime").comment("创建时间").datetime().commit()
        .addColumn().name("update_time").alias("updateTime").comment("修改时间").datetime().commit()
        .addColumn().name("status").alias("status").comment("状态").jdbcType(java.sql.JDBCType.TINYINT).commit()
        .comment("工作流环节自定义配置")
        .commit();
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