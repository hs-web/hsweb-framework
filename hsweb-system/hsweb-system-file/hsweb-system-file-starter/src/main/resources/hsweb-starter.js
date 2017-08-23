//组件信息
var info = {
    groupId: "${project.groupId}",
    artifactId: "${project.artifactId}",
    version: "${project.version}",
    website: "https://github.com/hs-web/hsweb-framework",
    author: "admin@hsweb.me",
    comment: "文件信息"
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
    database.createOrAlter("s_file_info")
        .addColumn().name("u_id").alias("id").comment("ID").jdbcType(java.sql.JDBCType.VARCHAR).length(32).primaryKey().commit()
        .addColumn().name("name").alias("name").comment("文件名称").jdbcType(java.sql.JDBCType.VARCHAR).length(256).commit()
        .addColumn().name("location").alias("location").comment("文件相对路径").jdbcType(java.sql.JDBCType.VARCHAR).length(1024).commit()
        .addColumn().name("type").alias("type").comment("类型").jdbcType(java.sql.JDBCType.VARCHAR).length(256).commit()
        .addColumn().name("md5").alias("md5").comment("md5校验值").jdbcType(java.sql.JDBCType.VARCHAR).length(256).commit()
        .addColumn().name("size").alias("size").comment("文件大小").jdbcType(java.sql.JDBCType.DECIMAL).length(32, 0).commit()
        .addColumn().name("status").alias("status").comment("状态").jdbcType(java.sql.JDBCType.DECIMAL).length(4, 0).commit()
        .addColumn().name("classified").alias("classified").comment("分类").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("create_time").alias("create_time").comment("创建时间").jdbcType(java.sql.JDBCType.NUMERIC).length(32,0).commit()
        .addColumn().name("creator_id").alias("creatorId").comment("创建人").jdbcType(java.sql.JDBCType.VARCHAR).length(256).commit()
        .comment("文件信息").commit();
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