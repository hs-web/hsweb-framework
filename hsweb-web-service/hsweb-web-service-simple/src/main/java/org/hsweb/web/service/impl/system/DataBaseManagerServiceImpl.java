package org.hsweb.web.service.impl.system;

import org.hsweb.ezorm.executor.EmptySQL;
import org.hsweb.ezorm.executor.SQL;
import org.hsweb.ezorm.executor.SqlExecutor;
import org.hsweb.ezorm.meta.DatabaseMetaData;
import org.hsweb.ezorm.meta.TableMetaData;
import org.hsweb.ezorm.meta.expand.SimpleMapWrapper;
import org.hsweb.ezorm.meta.parser.H2TableMetaParser;
import org.hsweb.ezorm.meta.parser.MysqlTableMetaParser;
import org.hsweb.ezorm.meta.parser.OracleTableMetaParser;
import org.hsweb.ezorm.meta.parser.TableMetaParser;
import org.hsweb.ezorm.render.SqlRender;
import org.hsweb.ezorm.render.dialect.H2DatabaseMeta;
import org.hsweb.ezorm.render.dialect.MysqlDatabaseMeta;
import org.hsweb.ezorm.render.dialect.OracleDatabaseMeta;
import org.hsweb.ezorm.render.support.simple.SimpleSQL;
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
 * Created by zhouhao on 16-4-21.
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
    public List<TableMetaData> getTableList() throws SQLException {
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
    public String createAlterSql(TableMetaData newTable) throws Exception {
        return createAlterSql(getDBType().getDatabaseMetaData(), getDBType().getTableMetaParser(sqlExecutor), newTable);
    }

    public String createAlterSql(DatabaseMetaData databaseMetaData, TableMetaParser tableMetaParser, TableMetaData newTable) throws Exception {
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
    public String createCreateSql(TableMetaData newTable) throws Exception {
        return createCreateSql(getDBType().getDatabaseMetaData(), newTable);
    }

    public String createCreateSql(DatabaseMetaData databaseMetaData, TableMetaData newTable) throws Exception {
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
            public DatabaseMetaData getDatabaseMetaData() {
                DatabaseMetaData databaseMetaData = new MysqlDatabaseMeta();
                databaseMetaData.init();
                return databaseMetaData;
            }
        },
        oracle {
            @Override
            public TableMetaParser getTableMetaParser(SqlExecutor sqlExecutor) {
                return new OracleTableMetaParser(sqlExecutor);
            }

            @Override
            public DatabaseMetaData getDatabaseMetaData() {
                DatabaseMetaData databaseMetaData = new OracleDatabaseMeta();
                databaseMetaData.init();
                return databaseMetaData;
            }
        },
        h2 {
            @Override
            public TableMetaParser getTableMetaParser(SqlExecutor sqlExecutor) {
                return new H2TableMetaParser(sqlExecutor);
            }

            @Override
            public DatabaseMetaData getDatabaseMetaData() {
                DatabaseMetaData databaseMetaData = new H2DatabaseMeta();
                databaseMetaData.init();
                return databaseMetaData;
            }
        };

        public abstract DatabaseMetaData getDatabaseMetaData();

        public abstract TableMetaParser getTableMetaParser(SqlExecutor sqlExecutor);
    }

}
