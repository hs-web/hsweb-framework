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
        .addColumn().name("permission_dimension").alias("permissionDimension").comment("启动权限配置").jdbcType(java.sql.JDBCType.CLOB).length(32).commit()
        .addColumn().name("properties").alias("properties").comment("其他配置").jdbcType(java.sql.JDBCType.CLOB).commit()
        .addColumn().name("create_time").alias("createTime").comment("创建时间").datetime().commit()
        .addColumn().name("update_time").alias("updateTime").comment("修改时间").datetime().commit()
        .addColumn().name("status").alias("status").comment("状态").jdbcType(java.sql.JDBCType.TINYINT).commit()
        .addColumn().name("listeners").alias("listeners").comment("监听器配置").jdbcType(java.sql.JDBCType.CLOB).commit()
        .comment("工作流流程自定义配置")
        .commit();

    database.createOrAlter("s_wf_act_conf")
        .addColumn().name("u_id").alias("id").comment("ID").jdbcType(java.sql.JDBCType.VARCHAR).length(32).primaryKey().commit()
        .addColumn().name("proc_def_key").alias("processDefineKey").comment("模板定义KEY").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("proc_def_id").alias("processDefineId").comment("模板定义ID").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("act_id").alias("activityId").comment("元图ID").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("form_id").alias("formId").comment("表单ID").length(32).jdbcType(java.sql.JDBCType.VARCHAR).commit()
        .addColumn().name("candidate_dimension").alias("candidateDimension").comment("候选人维度").jdbcType(java.sql.JDBCType.CLOB).length(32).commit()
        .addColumn().name("create_time").alias("createTime").comment("创建时间").datetime().commit()
        .addColumn().name("update_time").alias("updateTime").comment("修改时间").datetime().commit()
        .addColumn().name("status").alias("status").comment("状态").jdbcType(java.sql.JDBCType.TINYINT).commit()
        .addColumn().name("properties").alias("properties").comment("其他配置").jdbcType(java.sql.JDBCType.CLOB).commit()
        .addColumn().name("listeners").alias("listeners").comment("监听器配置").jdbcType(java.sql.JDBCType.CLOB).commit()
        .comment("工作流环节自定义配置")
        .commit();

    database.createOrAlter("s_wf_proc_his")
        .addColumn().name("u_id").alias("id").comment("ID").jdbcType(java.sql.JDBCType.VARCHAR).length(32).primaryKey().commit()
        .addColumn().name("proc_ins_id").alias("processInstanceId").comment("流程实例ID").notNull().jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("proc_def_id").alias("processDefineId").comment("模板定义ID").notNull().jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("type").alias("type").comment("类型").notNull().jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("type_text").alias("typeText").comment("类型说明").notNull().length(128).jdbcType(java.sql.JDBCType.VARCHAR).commit()
        .addColumn().name("task_id").alias("taskId").length(32).comment("任务ID").jdbcType(java.sql.JDBCType.VARCHAR).commit()
        .addColumn().name("task_name").alias("taskName").length(32).comment("任务名称").jdbcType(java.sql.JDBCType.VARCHAR).commit()
        .addColumn().name("task_def_key").alias("taskDefineKey").length(64).comment("任务定义KEY").jdbcType(java.sql.JDBCType.VARCHAR).commit()
        .addColumn().name("biz_key").alias("businessKey").length(32).comment("业务主键").jdbcType(java.sql.JDBCType.VARCHAR).commit()
        .addColumn().name("data").alias("data").comment("相关数据").jdbcType(java.sql.JDBCType.CLOB).commit()
        .addColumn().name("create_time").alias("createTime").notNull().comment("创建时间").datetime().commit()
        .addColumn().name("creator_id").alias("creatorId").length(32).notNull().comment("创建人ID").jdbcType(java.sql.JDBCType.VARCHAR).commit()
        .addColumn().name("creator_name").alias("creatorName").length(32).notNull().comment("创建人姓名").jdbcType(java.sql.JDBCType.VARCHAR).commit()
        .comment("工作流流程历史")
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