package org.hswebframework.web.database.manager.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hswebframework.web.Sqls;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.exception.AccessDenyException;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.database.manager.DatabaseManagerService;
import org.hswebframework.web.database.manager.SqlExecuteRequest;
import org.hswebframework.web.database.manager.SqlExecuteResult;
import org.hswebframework.web.database.manager.SqlInfo;
import org.hswebframework.web.database.manager.meta.ObjectMetadata;
import org.hswebframework.web.database.manager.sql.TransactionInfo;
import org.hswebframework.web.datasource.DataSourceHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/database/manager")
@Api(tags = "开发人员工具-数据库维护", value = "数据库维护")
@Authorize(permission = "database-manager", description = "数据库维护")
public class DataBaseManagerController {

    @Autowired
    private DatabaseManagerService databaseManagerService;

    @GetMapping("/metas")
    @Authorize(action = Permission.ACTION_QUERY, description = "获取元数据")
    @ApiOperation("获取数据库元数据")
    public ResponseMessage<Map<ObjectMetadata.ObjectType, List<? extends ObjectMetadata>>> parseAllObject() throws Exception {
        return parseAllObject(null);
    }

    @GetMapping("/metas/{datasourceId}")
    @Authorize(action = Permission.ACTION_QUERY, description = "获取元数据")
    @ApiOperation("获取指定数据源的元数据")
    public ResponseMessage<Map<ObjectMetadata.ObjectType, List<? extends ObjectMetadata>>> parseAllObject(
            @PathVariable
            @ApiParam("数据源ID") String datasourceId) throws Exception {

        DataSourceHolder.switcher().use(datasourceId);
        return ResponseMessage.ok(databaseManagerService.getMetas());
    }

    @PostMapping(value = "/execute/{datasourceId}", consumes = MediaType.TEXT_PLAIN_VALUE)
    @Authorize(action = "execute", description = "执行SQL")
    @ApiOperation(value = "指定数据源执行SQL")
    public ResponseMessage<List<SqlExecuteResult>> execute(
            @PathVariable @ApiParam("数据源ID") String datasourceId,
            @RequestBody @ApiParam("SQL脚本") String sqlLines) throws Exception {
        DataSourceHolder.switcher().use(datasourceId);
        return ResponseMessage.ok(databaseManagerService.execute(SqlExecuteRequest.builder()
                .sql(parseSql(sqlLines, datasourceId))
                .build()));

    }

    @PostMapping(value = "/execute", consumes = MediaType.TEXT_PLAIN_VALUE)
    @ApiOperation(value = "执行SQL")
    @Authorize(action = "execute", description = "执行SQL")
    public ResponseMessage<List<SqlExecuteResult>> execute(@RequestBody
                                                           @ApiParam("SQL脚本") String sqlLines) throws Exception {
        return ResponseMessage.ok(databaseManagerService
                .execute(SqlExecuteRequest.builder()
                        .sql(parseSql(sqlLines, null))
                        .build()));
    }

    @PostMapping(value = "/transactional/execute/{transactionalId}", consumes = MediaType.TEXT_PLAIN_VALUE)
    @Authorize(action = "execute", description = "执行SQL")
    @ApiOperation(value = "开启事务执行SQL")
    public ResponseMessage<List<SqlExecuteResult>> executeTransactional(@PathVariable @ApiParam("事务ID") String transactionalId,
                                                                        @ApiParam("SQL脚本") @RequestBody String sqlLines) throws Exception {
        return ResponseMessage.ok(databaseManagerService.execute(transactionalId, SqlExecuteRequest.builder()
                .sql(parseSql(sqlLines, null))
                .build()));
    }

    @PostMapping(value = "/transactional/execute/{transactionalId}/{dataSourceId}", consumes = MediaType.TEXT_PLAIN_VALUE)
    @Authorize(action = "execute", description = "执行SQL")
    @ApiOperation(value = "开启事务执行指定数据源对SQL")
    public ResponseMessage<List<SqlExecuteResult>> executeTransactional(@PathVariable @ApiParam("事务ID") String transactionalId,
                                                                        @PathVariable @ApiParam("数据源ID") String dataSourceId,
                                                                        @ApiParam("SQL脚本") @RequestBody String sqlLines) throws Exception {
        DataSourceHolder.switcher().use(dataSourceId);
        return ResponseMessage.ok(databaseManagerService.execute(transactionalId, SqlExecuteRequest.builder()
                .sql(parseSql(sqlLines, dataSourceId))
                .build()));
    }

    @GetMapping("/transactional/new")
    @Authorize(action = "execute", description = "执行SQL")
    @ApiOperation("新建事务")
    public ResponseMessage<String> newTransaction() throws Exception {
        return ResponseMessage.ok(databaseManagerService.newTransaction());
    }

    @GetMapping("/transactional/new/{dataSourceId}")
    @Authorize(action = "execute", description = "执行SQL")
    @ApiOperation("指定数据源新建事务")
    public ResponseMessage<String> newTransaction(@PathVariable String dataSourceId) throws Exception {
        DataSourceHolder.switcher().use(dataSourceId);
        return ResponseMessage.ok(databaseManagerService.newTransaction(dataSourceId));
    }


    @GetMapping("/transactional")
    @Authorize(action = "execute", description = "执行SQL")
    @ApiOperation("获取全部事务信息")
    public ResponseMessage<List<TransactionInfo>> allTransaction() throws Exception {
        return ResponseMessage.ok(databaseManagerService.allTransaction());
    }

    @PostMapping("/transactional/{id}/commit")
    @Authorize(action = "execute", description = "执行SQL")
    @ApiOperation("提交事务")
    public ResponseMessage<String> commitTransaction(@PathVariable String id) throws Exception {
        databaseManagerService.commit(id);
        return ResponseMessage.ok();
    }

    @PostMapping("/transactional/{id}/rollback")
    @Authorize(action = "execute", description = "执行SQL")
    @ApiOperation("回滚事务")
    public ResponseMessage<String> rollbackTransaction(@PathVariable String id) throws Exception {
        databaseManagerService.rollback(id);
        return ResponseMessage.ok();
    }


    private List<SqlInfo> parseSql(String sqlText, String datasourceId) {
      //  Authentication authentication = Authentication.current().orElse(null);

        List<String> sqlList = Sqls.parse(sqlText);
        return sqlList.stream().map(sql -> {
            SqlInfo sqlInfo = new SqlInfo();
            sqlInfo.setSql(sql);
            sqlInfo.setDatasourceId(datasourceId);
            sqlInfo.setType(sql.split("[ ]")[0].toLowerCase());
//            if (authentication != null) {
//                if (!authentication.hasPermission("database-manager", sqlInfo.getType())) {
//
//                   // throw new AccessDenyException("权限不足");
//                }
//            }
            return sqlInfo;
        }).collect(Collectors.toList());
    }

}
