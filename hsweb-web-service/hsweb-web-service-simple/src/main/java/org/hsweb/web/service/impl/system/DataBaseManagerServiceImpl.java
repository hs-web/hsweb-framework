package org.hsweb.web.service.impl.system;

import org.hsweb.ezorm.rdb.executor.EmptySQL;
import org.hsweb.ezorm.rdb.executor.SQL;
import org.hsweb.ezorm.rdb.executor.SqlExecutor;
import org.hsweb.ezorm.rdb.meta.RDBDatabaseMetaData;
import org.hsweb.ezorm.rdb.meta.RDBTableMetaData;
import org.hsweb.ezorm.rdb.meta.expand.SimpleMapWrapper;
import org.hsweb.ezorm.rdb.meta.parser.H2TableMetaParser;
import org.hsweb.ezorm.rdb.meta.parser.MysqlTableMetaParser;
import org.hsweb.ezorm.rdb.meta.parser.OracleTableMetaParser;
import org.hsweb.ezorm.rdb.meta.parser.TableMetaParser;
import org.hsweb.ezorm.rdb.render.SqlRender;
import org.hsweb.ezorm.rdb.render.dialect.H2RDBDatabaseMetaData;
import org.hsweb.ezorm.rdb.render.dialect.MysqlRDBDatabaseMetaData;
import org.hsweb.ezorm.rdb.render.dialect.OracleRDBDatabaseMetaData;
import org.hsweb.ezorm.rdb.render.support.simple.SimpleSQL;
import org.hsweb.web.core.datasource.DataSourceHolder;
import org.hsweb.web.service.system.DataBaseManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 数据库管理服务实现类
 *
 * @author zhouhao,
 * @version 1.0
 * @see DataBaseManagerService
 */
@Service(value = "dataBaseManagerService")
public class DataBaseManagerServiceImpl implements DataBaseManagerService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Resource
    private SqlExecutor sqlExecutor;

    @Override
    @Transactional(readOnly = true)
    public List<RDBTableMetaData> getTableList() throws SQLException {
        return getDBType().getTableMetaParser(sqlExecutor).parseAll();
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public List<Map<String, Object>> execSql(List<String> sqlList) throws SQLException {
        return execSql(sqlExecutor, sqlList);
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<Map<String, Object>> execSql(SqlExecutor sqlExecutor, List<String> sqlList) throws SQLException {
        List<Map<String, Object>> response = new LinkedList<>();
        for (String s : sqlList) {
            Map<String, Object> msg = new LinkedHashMap<>();
            msg.put("sql", s);
            String type = s.split("[ ]")[0];
            msg.put("type", type.toLowerCase());
            switch (type.toLowerCase()) {
                case "delete":
                    msg.put("total", sqlExecutor.delete(new SimpleSQL(s)));
                    break;
                case "insert":
                    msg.put("total", sqlExecutor.insert(new SimpleSQL(s)));
                    break;
                case "update":
                    msg.put("total", sqlExecutor.update(new SimpleSQL(s)));
                    break;
                case "select":
                    List<Map<String, Object>> data = sqlExecutor.list(new SimpleSQL(s), new SimpleMapWrapper() {
                        @Override
                        public void setUp(List<String> columns) {
                            msg.put("columns", columns);
                        }
                    });
                    msg.put("data", data);
                    break;
                default:
                    sqlExecutor.exec(new SimpleSQL(s));
                    msg.put("total", 1);
                    break;
            }
            response.add(msg);
        }
        return response;
    }

    @Override
    public String createAlterSql(RDBTableMetaData newTable) throws Exception {
        return createAlterSql(getDBType().getDatabaseMetaData(), getDBType().getTableMetaParser(sqlExecutor), newTable);
    }

    public String createAlterSql(RDBDatabaseMetaData databaseMetaData, TableMetaParser tableMetaParser, RDBTableMetaData newTable) throws Exception {
        databaseMetaData.putTable(tableMetaParser.parse(newTable.getName()));
        SQL sql = databaseMetaData.getRenderer(SqlRender.TYPE.META_ALTER).render(newTable, true);
        if (sql instanceof EmptySQL) return "";
        StringBuilder builder = new StringBuilder(sql.getSql());
        builder.append(";\n");
        if (sql.getBinds() != null && !sql.getBinds().isEmpty())
            sql.getBinds().forEach(bindSQL -> builder.append(bindSQL.getSql().getSql()).append(";\n"));
        return builder.toString();
    }

    @Override
    public String createCreateSql(RDBTableMetaData newTable) throws Exception {
        return createCreateSql(getDBType().getDatabaseMetaData(), newTable);
    }

    public String createCreateSql(RDBDatabaseMetaData databaseMetaData, RDBTableMetaData newTable) throws Exception {
        SQL sql = databaseMetaData.getRenderer(SqlRender.TYPE.META_CREATE).render(newTable, true);
        if (sql instanceof EmptySQL) return "";
        StringBuilder builder = new StringBuilder(sql.getSql());
        builder.append(";\n");
        if (sql.getBinds() != null && !sql.getBinds().isEmpty())
            sql.getBinds().forEach(bindSQL -> builder.append(bindSQL.getSql().getSql()).append(";\n"));
        return builder.toString();
    }

    public DBType getDBType() {
        return DBType.valueOf(DataSourceHolder.getActiveDatabaseType().name());
    }

    enum DBType {
        mysql {
            @Override
            public TableMetaParser getTableMetaParser(SqlExecutor sqlExecutor) {
                return new MysqlTableMetaParser(sqlExecutor);
            }

            @Override
            public RDBDatabaseMetaData getDatabaseMetaData() {
                return new MysqlRDBDatabaseMetaData();
            }
        },
        oracle {
            @Override
            public TableMetaParser getTableMetaParser(SqlExecutor sqlExecutor) {
                return new OracleTableMetaParser(sqlExecutor);
            }

            @Override
            public RDBDatabaseMetaData getDatabaseMetaData() {
                return new OracleRDBDatabaseMetaData();
            }
        },
        h2 {
            @Override
            public TableMetaParser getTableMetaParser(SqlExecutor sqlExecutor) {
                return new H2TableMetaParser(sqlExecutor);
            }

            @Override
            public RDBDatabaseMetaData getDatabaseMetaData() {
                return new H2RDBDatabaseMetaData();
            }
        };

        public abstract RDBDatabaseMetaData getDatabaseMetaData();

        public abstract TableMetaParser getTableMetaParser(SqlExecutor sqlExecutor);
    }

}
