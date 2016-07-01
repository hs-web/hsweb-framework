package org.hsweb.web.controller.system;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.hsweb.ezorm.meta.DatabaseMetaData;
import org.hsweb.ezorm.meta.FieldMetaData;
import org.hsweb.ezorm.meta.TableMetaData;
import org.hsweb.ezorm.render.SqlAppender;
import org.hsweb.ezorm.render.SqlRender;
import org.hsweb.web.bean.po.user.User;
import org.hsweb.web.core.authorize.annotation.Authorize;
import org.hsweb.web.core.exception.AuthorizeException;
import org.hsweb.web.core.exception.AuthorizeForbiddenException;
import org.hsweb.web.core.message.ResponseMessage;
import org.hsweb.web.core.utils.WebUtil;
import org.hsweb.web.service.form.DynamicFormService;
import org.hsweb.web.service.system.DataBaseManagerService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by zhouhao on 16-6-30.
 */
@RestController
@RequestMapping("/database")
@Authorize(module = "database")
public class DatabaseManagerController {
    @Resource
    private DataBaseManagerService dataBaseManagerService;

    @RequestMapping(value = "/tables", method = RequestMethod.GET)
    @Authorize(action = "R")
    public ResponseMessage showTables() throws SQLException {
        return ResponseMessage.ok(dataBaseManagerService.getTableList())
                .include(TableMetaData.class, "name", "alias", "comment", "fields")
                .include(FieldMetaData.class, "name", "alias", "comment", "dataType", "properties")
                .onlyData();
    }

    @RequestMapping(value = "/exec", method = RequestMethod.POST)
    public ResponseMessage exec(@RequestBody String sql) throws Exception {
        String[] list = sql.split("[\n]");
        List<SqlAppender> sqlList = new LinkedList<>();
        SqlAppender[] tmp = {new SqlAppender()};
        Arrays.asList(list).stream()
                .filter(s -> !s.startsWith("--") && s.trim().length() != 0)
                .forEach(s1 -> {
                    if (s1.trim().endsWith(";")) {
                        s1 = s1.trim();
                        tmp[0].add(s1.substring(0, s1.length() - 1));
                        sqlList.add(tmp[0]);
                        tmp[0] = new SqlAppender();
                    } else {
                        tmp[0].add(s1);
                    }
                });
        if (!tmp[0].isEmpty()) sqlList.add(tmp[0]);
        List<String> sqlStringList = new ArrayList<>();
        User user = WebUtil.getLoginUser();
        for (SqlAppender appender : sqlList) {
            String sqlLine = appender.toString().trim();
            String type = sqlLine.split("[ ]")[0];
            if (!user.hasAccessModuleAction("database", type.toLowerCase()))
                throw new AuthorizeForbiddenException("权限不足");
            sqlStringList.add(sqlLine);
        }
        return ResponseMessage.ok(dataBaseManagerService.execSql(sqlStringList));
    }

    @RequestMapping(value = "/sql/alter", method = RequestMethod.POST)
    public ResponseMessage showAlterSql(@RequestBody JSONObject jsonObject) throws Exception {
        return ResponseMessage.ok(dataBaseManagerService.createAlterSql(createTableMetaDataByJson(jsonObject)));
    }

    @RequestMapping(value = "/sql/create", method = RequestMethod.POST)
    public ResponseMessage showCreateSql(@RequestBody JSONObject jsonObject) throws Exception {
        return ResponseMessage.ok(dataBaseManagerService.createCreateSql(createTableMetaDataByJson(jsonObject)));
    }

    protected TableMetaData createTableMetaDataByJson(JSONObject jsonObject) {
        TableMetaData tableMetaData = new TableMetaData();
        tableMetaData.setName(jsonObject.getString("name"));
        tableMetaData.setComment(jsonObject.getString("comment"));
        JSONArray jsonArray = jsonObject.getJSONArray("fields");
        for (int i = 0; i < jsonArray.size(); i++) {
            FieldMetaData field = jsonArray.getObject(i, FieldMetaData.class);
            tableMetaData.addField(field);
        }
        return tableMetaData;
    }
}
