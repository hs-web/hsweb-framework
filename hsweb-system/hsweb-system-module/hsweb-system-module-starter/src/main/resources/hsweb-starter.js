
//组件信息
var info = {
    groupId: "${project.groupId}",
    artifactId: "${project.artifactId}",
    version: "${project.version}",
    website: "https://github.com/hs-web/hsweb-framework",
    author: "admin@hsweb.me",
    comment: "系统自定义模块"
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
    database.createOrAlter("s_module_meta")
        .addColumn().name("u_id").alias("id").comment("ID").jdbcType(java.sql.JDBCType.VARCHAR).length(32).primaryKey().commit()
        .addColumn().name("name").alias("name").comment("名称").jdbcType(java.sql.JDBCType.VARCHAR).length(256).commit()
        .addColumn().name("permission_id").alias("permissionId").comment("权限id").jdbcType(java.sql.JDBCType.VARCHAR).length(32).commit()
        .addColumn().name("remark").alias("remark").comment("备注").jdbcType(java.sql.JDBCType.VARCHAR).length(1024).commit()
        .addColumn().name("list_meta").alias("listMeta").comment("列表配置").jdbcType(java.sql.JDBCType.CLOB).commit()
        .addColumn().name("save_meta").alias("saveMeta").comment("保存页配置").jdbcType(java.sql.JDBCType.CLOB).commit()
        .addColumn().name("status").alias("status").comment("状态").jdbcType(java.sql.JDBCType.DECIMAL).length(4,0).commit()
        .comment("系统自定义模块").commit();
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