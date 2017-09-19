//组件信息
var info = {
    groupId: "${project.groupId}",
    artifactId: "${project.artifactId}",
    version: "${project.version}",
    website: "https://github.com/hs-web/hsweb-framework",
    author: "admin@hsweb.me",
    comment: "动态脚本"
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
    database.createOrAlter("s_script")
        .addColumn().name("u_id").alias("id").comment("ID").jdbcType(java.sql.JDBCType.VARCHAR).length(32).primaryKey().commit()
        .addColumn().name("name").alias("name").comment("脚本名称").jdbcType(java.sql.JDBCType.VARCHAR).length(256).commit()
        .addColumn().name("type").alias("type").comment("类型").jdbcType(java.sql.JDBCType.VARCHAR).length(256).commit()
        .addColumn().name("script").alias("script").comment("脚本内容").jdbcType(java.sql.JDBCType.CLOB).commit()
        .addColumn().name("language").alias("language").comment("脚本语言").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("remark").alias("remark").comment("备注").jdbcType(java.sql.JDBCType.VARCHAR).length(512).commit()
        .addColumn().name("status").alias("status").comment("状态").jdbcType(java.sql.JDBCType.DECIMAL).length(4, 0).commit()
        .addColumn().name("tag").alias("tag").comment("脚本标签").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .comment("动态脚本").commit();

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