//组件信息
var info = {
    groupId: "${project.groupId}",
    artifactId: "${project.artifactId}",
    version: "${project.version}",
    website: "https://github.com/hs-web/hsweb-framework",
    author: "admin@hsweb.me",
    comment: "数据源配置"
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
    database.createOrAlter("s_datasource_conf")
        .addColumn().name("u_id").alias("id").comment("ID").jdbcType(java.sql.JDBCType.VARCHAR).length(32).primaryKey().commit()
        .addColumn().name("name").alias("name").comment("数据源名称").jdbcType(java.sql.JDBCType.VARCHAR).length(64).commit()
        .addColumn().name("enabled").alias("enabled").comment("是否启用").jdbcType(java.sql.JDBCType.DECIMAL).length(4, 0).commit()
        .addColumn().name("create_date").alias("createDate").comment("创建日期").jdbcType(java.sql.JDBCType.DATE).commit()
        .addColumn().name("properties").alias("properties").comment("数据源配置").jdbcType(java.sql.JDBCType.CLOB).commit()
        .addColumn().name("describe").alias("describe").comment("备注").jdbcType(java.sql.JDBCType.VARCHAR).length(512).commit()
        .comment("数据源配置").commit();
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