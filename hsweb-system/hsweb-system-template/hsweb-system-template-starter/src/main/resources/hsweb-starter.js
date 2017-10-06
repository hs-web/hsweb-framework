//组件信息
var info = {
    groupId: "${project.groupId}",
    artifactId: "${project.artifactId}",
    version: "${project.version}",
    website: "https://github.com/hs-web/hsweb-framework",
    author: "admin@hsweb.me",
    comment: "模板"
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
    database.createOrAlter("s_template")
        .addColumn().name("u_id").alias("id").comment("ID").jdbcType(java.sql.JDBCType.VARCHAR).length(32).primaryKey().commit()
        .addColumn().name("name").alias("name").comment("模板名称").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("type").alias("type").comment("模板类型").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("template").alias("template").comment("模板内容").jdbcType(java.sql.JDBCType.CLOB).commit()
        .addColumn().name("config").alias("config").comment("模板配置").jdbcType(java.sql.JDBCType.CLOB).commit()
        .addColumn().name("version").alias("version").comment("版本号").jdbcType(java.sql.JDBCType.DECIMAL).length(32, 0).commit()
        .addColumn().name("classified").alias("classified").comment("模板分类").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .comment("模板").commit();
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