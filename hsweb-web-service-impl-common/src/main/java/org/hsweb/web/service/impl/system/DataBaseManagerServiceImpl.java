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
import org.hsweb.ezorm.run.simple.SimpleDatabase;
import org.hsweb.web.core.Install;
import org.hsweb.web.core.exception.BusinessException;
import org.hsweb.web.core.exception.NotFoundException;
import org.hsweb.web.service.datasource.DataSourceService;
import org.hsweb.web.service.datasource.DynamicDataSourceService;
import org.hsweb.web.service.impl.DatabaseMetaDataFactoryBean;
import org.hsweb.web.service.system.DataBaseManagerService;
import org.hsweb.web.service.system.SqlExecuteProcess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;
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

    @Resource
    private SqlExecutor sqlExecutor;

    @Autowired(required = false)
    private TableMetaParser tableMetaParser;

    @Autowired
    private DatabaseMetaDataFactoryBean databaseMetaDataFactoryBean;

    @Resource
    private DynamicDataSourceService dynamicDataSourceService;

    @Resource
    private DataSourceService dataSourceService;

    @Override
    public List<TableMetaData> getTableList() throws SQLException {
        if (tableMetaParser == null) {
            throw new BusinessException("不支持的数据库");
        }
        return tableMetaParser.parseAll();
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public List<Map<String, Object>> execSql(List<String> sqlList) throws SQLException {
        return execSql(sqlExecutor, sqlList);
    }

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
        return createAlterSql(databaseMetaDataFactoryBean.getObject(), tableMetaParser, newTable);
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
        return createCreateSql(databaseMetaDataFactoryBean.getObject(), newTable);
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

    @Override
    public List<TableMetaData> getTableList(String datasourceId) throws SQLException {
        SqlExecutor sqlExecutor = dynamicDataSourceService.getSqlExecutor(datasourceId);
        DBType dbType = getDBType(datasourceId);
        return dbType.getTableMetaParser(sqlExecutor).parseAll();
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public List<Map<String, Object>> execSql(String datasourceId, List<String> sqlList) throws SQLException {
        return execSql(dynamicDataSourceService.getSqlExecutor(datasourceId), sqlList);
    }

    @Override
    public String createAlterSql(String datasourceId, TableMetaData newTable) throws Exception {
        DBType dbType = getDBType(datasourceId);
        SqlExecutor sqlExecutor = dynamicDataSourceService.getSqlExecutor(datasourceId);
        return createAlterSql(dbType.getDatabaseMetaData(), dbType.getTableMetaParser(sqlExecutor), newTable);
    }

    @Override
    public String createCreateSql(String datasourceId, TableMetaData newTable) throws Exception {
        return createCreateSql(getDBType(datasourceId).getDatabaseMetaData(), newTable);
    }

    public DBType getDBType(String datasourceId) {
        org.hsweb.web.bean.po.datasource.DataSource dataSource = dataSourceService.selectByPk(datasourceId);
        String driver = dataSource.getDriver();
        if (driver.contains("mysql")) {
            return DBType.mysql;
        }
        if (driver.contains("oracle")) {
            return DBType.oracle;
        }
        if (driver.contains("h2")) {
            return DBType.h2;
        }
        throw new NotFoundException(driver);
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
        }, h2 {
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
