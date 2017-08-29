
//组件信息
var info = {
    groupId: "${project.groupId}",
    artifactId: "${project.artifactId}",
    version: "${project.version}",
    website: "https://github.com/hs-web/hsweb-framework",
    author: "admin@hsweb.me",
    comment: "调度任务"
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
    database.createOrAlter("s_schedule_job")
        .addColumn().name("u_id").alias("id").comment("ID").jdbcType(java.sql.JDBCType.VARCHAR).length(32).primaryKey().commit()
        .addColumn().name("name").alias("name").comment("任务名称").jdbcType(java.sql.JDBCType.VARCHAR).length(128).commit()
        .addColumn().name("remark").alias("remark").comment("备注").jdbcType(java.sql.JDBCType.VARCHAR).length(512).commit()
        .addColumn().name("quartz_config").alias("quartzConfig").comment("定时调度配置").jdbcType(java.sql.JDBCType.CLOB).commit()
        .addColumn().name("script").alias("script").comment("执行脚本").jdbcType(java.sql.JDBCType.CLOB).commit()
        .addColumn().name("language").alias("language").comment("脚本语言").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("status").alias("status").comment("是否启用").jdbcType(java.sql.JDBCType.DECIMAL).length(4,0).commit()
        .addColumn().name("parameters").alias("parameters").comment("启动参数").jdbcType(java.sql.JDBCType.CLOB).commit()
        .addColumn().name("type").alias("type").comment("任务类型").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("tags").alias("tags").comment("标签").jdbcType(java.sql.JDBCType.VARCHAR).length(512).commit()
        .comment("调度任务").commit();

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