import org.hsweb.ezorm.rdb.RDBDatabase

import java.sql.JDBCType;

database.createOrAlter("s_oauth2_access")
        .addColumn().name("u_id").jdbcType(JDBCType.VARCHAR).length(32).notNull().primaryKey().comment("ID").commit()
        .addColumn().name("client_id").jdbcType(JDBCType.VARCHAR).length(32).notNull().comment("客户端ID").commit()
        .addColumn().name("user_id").jdbcType(JDBCType.VARCHAR).length(32).notNull().comment("用户ID").commit()
        .addColumn().name("access_token").jdbcType(JDBCType.VARCHAR).length(128).notNull().comment("授权码").commit()
        .addColumn().name("refresh_token").jdbcType(JDBCType.VARCHAR).length(128).notNull().comment("授权更新码").commit()
        .addColumn().name("expire_in").number(32).notNull().comment("有效期").commit()
        .addColumn().name("create_date").datetime().notNull().comment("创建日期").commit()
        .comment("OAuth2授权信息").commit()

database.createOrAlter("s_oauth2_client")
        .addColumn().name("u_id").jdbcType(JDBCType.VARCHAR).length(32).notNull().primaryKey().comment("ID").commit()
        .addColumn().name("user_id").jdbcType(JDBCType.VARCHAR).length(32).notNull().comment("用户ID").commit()
        .addColumn().name("name").jdbcType(JDBCType.VARCHAR).length(128).comment("客户端名称").commit()
        .addColumn().name("secret").jdbcType(JDBCType.VARCHAR).length(128).comment("密钥").commit()
        .addColumn().name("comment").jdbcType(JDBCType.VARCHAR).length(512).comment("备注").commit()
        .addColumn().name("status").integer().comment("状态").commit()
        .comment("OAuth2客户端信息").commit()