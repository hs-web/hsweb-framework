package org.hsweb.web.service.impl.system;

import org.hsweb.ezorm.executor.SqlExecutor;
import org.hsweb.ezorm.meta.TableMetaData;
import org.hsweb.ezorm.meta.parser.MysqlTableMetaParser;
import org.hsweb.ezorm.meta.parser.OracleTableMetaParser;
import org.hsweb.ezorm.meta.parser.TableMetaParser;
import org.hsweb.web.core.Install;
import org.hsweb.web.core.exception.BusinessException;
import org.hsweb.web.service.system.DataBaseManagerService;
import org.hsweb.web.service.system.SqlExecuteProcess;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.List;

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

    private TableMetaParser tableMetaParser;

    @PostConstruct
    public void init() {
        switch (Install.getDatabaseType()) {
            case "mysql":
                tableMetaParser = new MysqlTableMetaParser(sqlExecutor);
                break;
            case "oracle":
                tableMetaParser = new OracleTableMetaParser(sqlExecutor);
                break;
        }
    }

    @Override
    public List<TableMetaData> getTableList() throws SQLException {
        if (tableMetaParser == null) {
            throw new BusinessException("表结构解析器不支持");
        }
        return tableMetaParser.parseAll();
    }

    @Override
    public void executeSQL(String sql, SqlExecuteProcess process) throws SQLException {

    }


}
