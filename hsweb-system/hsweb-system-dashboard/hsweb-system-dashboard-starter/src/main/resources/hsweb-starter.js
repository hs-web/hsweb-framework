//组件信息
var info = {
    groupId: "${project.groupId}",
    artifactId: "${project.artifactId}",
    version: "${project.version}",
    website: "https://github.com/hs-web/hsweb-framework",
    author: "admin@hsweb.me",
    comment: "仪表盘配置"
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
    database.createOrAlter("s_dashboard_conf")
        .addColumn().name("u_id").alias("id").comment("ID").jdbcType(java.sql.JDBCType.VARCHAR).length(32).primaryKey().commit()
        .addColumn().name("name").alias("name").comment("配置名称").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("type").alias("type").comment("配置类型").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("template").alias("template").comment("模板").jdbcType(java.sql.JDBCType.CLOB).commit()
        .addColumn().name("script").alias("script").comment("脚本").jdbcType(java.sql.JDBCType.CLOB).commit()
        .addColumn().name("script_lang").alias("scriptLanguage").comment("脚本语言").varchar(32).commit()
        .addColumn().name("permission").alias("permission").comment("权限设置").varchar(512).commit()
        .addColumn().name("creator_id").alias("creatorId").comment("创建人").varchar(32).commit()
        .addColumn().name("create_time").alias("createTime").comment("创建时间").number(32).commit()
        .addColumn().name("sort_index").alias("sortIndex").comment("排序").number(32).commit()
        .addColumn().name("status").alias("status").comment("状态").number(4).commit()
        .addColumn().name("is_default").alias("defaultConfig").comment("是否默认").number(2).commit()
        .comment("仪表盘配置").commit();
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