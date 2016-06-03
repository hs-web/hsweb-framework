package org.hsweb.web.service.impl.form;

import com.alibaba.fastjson.JSON;
import org.hsweb.concurrent.lock.annotation.LockName;
import org.hsweb.concurrent.lock.annotation.ReadLock;
import org.hsweb.concurrent.lock.annotation.WriteLock;
import org.hsweb.web.bean.common.*;
import org.hsweb.web.bean.po.GenericPo;
import org.hsweb.web.bean.po.form.Form;
import org.hsweb.web.bean.po.history.History;
import org.hsweb.web.core.Install;
import org.hsweb.web.core.authorize.ExpressionScopeBean;
import org.hsweb.web.core.exception.BusinessException;
import org.hsweb.web.core.exception.NotFoundException;
import org.hsweb.web.service.form.DynamicFormDataValidator;
import org.hsweb.web.service.form.DynamicFormService;
import org.hsweb.web.service.form.FormService;
import org.hsweb.web.service.history.HistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.webbuilder.office.excel.ExcelIO;
import org.webbuilder.office.excel.config.Header;
import org.webbuilder.sql.*;
import org.webbuilder.sql.exception.CreateException;
import org.webbuilder.sql.exception.TriggerException;
import org.webbuilder.sql.param.ExecuteCondition;
import org.webbuilder.sql.trigger.TriggerResult;
import org.webbuilder.utils.common.StringUtils;

import javax.annotation.Resource;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * Created by zhouhao on 16-4-14.
 */
@Service("dynamicFormService")
@Transactional(rollbackFor = Throwable.class)
public class DynamicFormServiceImpl implements DynamicFormService, ExpressionScopeBean {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired(required = false)
    protected FormParser formParser;

    @Autowired
    protected DataBase dataBase;

    @Resource
    protected FormService formService;

    @Resource
    protected HistoryService historyService;

    @Autowired(required = false)
    protected List<DynamicFormDataValidator> dynamicFormDataValidator;

    @Autowired(required = false)
    protected Map<String, ExpressionScopeBean> expressionScopeBeanMap;

    protected void initDefaultField(TableMetaData metaData) {
        String dataType;
        switch (Install.getDatabaseType()) {
            case "oracle":
                dataType = "varchar2(32)";
                break;
            case "h2":
                dataType = "varchar2(32)";
                break;
            default:
                dataType = "varchar(32)";
        }
        FieldMetaData id = new FieldMetaData("u_id", String.class, dataType);
        id.setPrimaryKey(true);
        id.setNotNull(true);
        id.setComment("主键");
        metaData.attr("primaryKey", "u_id");
        metaData.addField(id);

    }

    @Override
    public Object parseMeta(Form form) throws Exception {
        return formParser.parse(form);
    }

    @Override
    @WriteLock
    @LockName(value = "'form.lock.'+#form.name", isExpression = true)
    public void deploy(Form form) throws Exception {
        TableMetaData metaData = formParser.parse(form);
        initDefaultField(metaData);
        History history = historyService.selectLastHistoryByType("form.deploy." + form.getName());
        //首次部署
        if (history == null) {
            try {
                dataBase.createTable(metaData);
            } catch (CreateException e) {
                dataBase.updateTable(metaData);
            }
        } else {
            Form lastDeploy = JSON.parseObject(history.getChangeAfter(), Form.class);
            TableMetaData lastDeployMetaData = formParser.parse(lastDeploy);
            initDefaultField(lastDeployMetaData);
            //向上发布
            dataBase.updateTable(lastDeployMetaData);//先放入旧的结构
            //更新结构
            dataBase.alterTable(metaData);
        }
    }

    @Override
    @WriteLock
    @LockName(value = "'form.lock.'+#form.name", isExpression = true)
    public void unDeploy(Form form) throws Exception {
        dataBase.removeTable(form.getName());
    }

    public Table getTableByName(String name) throws Exception {
        Table table = dataBase.getTable(name.toUpperCase());
        if (table == null)
            table = dataBase.getTable(name.toLowerCase());
        if (table == null) {
            throw new NotFoundException("表单[" + name + "]不存在");
        }
        return table;
    }

    @Override
    @ReadLock
    @LockName(value = "'form.lock.'+#name", isExpression = true)
    @Transactional(readOnly = true)
    public <T> PagerResult<T> selectPager(String name, QueryParam param) throws Exception {
        PagerResult<T> result = new PagerResult<>();
        Table table = getTableByName(name);
        Query query = table.createQuery();
        QueryParamProxy proxy = QueryParamProxy.build(param);
        int total = query.total(proxy);
        result.setTotal(total);
        param.rePaging(total);
        proxy = QueryParamProxy.build(param);
        result.setData(query.list(proxy));
        return result;
    }

    @Override
    @ReadLock
    @LockName(value = "'form.lock.'+#name", isExpression = true)
    @Transactional(readOnly = true)
    public <T> List<T> select(String name, QueryParam param) throws Exception {
        Table table = getTableByName(name);
        Query query = table.createQuery();
        param.setPaging(false);
        QueryParamProxy proxy = QueryParamProxy.build(param);
        return query.list(proxy);
    }

    @Override
    @ReadLock
    @LockName(value = "'form.lock.'+#name", isExpression = true)
    @Transactional(readOnly = true)
    public int total(String name, QueryParam param) throws Exception {
        Table table = getTableByName(name);
        Query query = table.createQuery();
        param.setPaging(false);
        QueryParamProxy proxy = QueryParamProxy.build(param);
        return query.total(proxy);
    }

    @Override
    @ReadLock
    @LockName(value = "'form.lock.'+#name", isExpression = true)
    public String insert(String name, InsertParam<Map<String, Object>> param) throws Exception {
        Table table = getTableByName(name);
        Insert insert = table.createInsert();
        InsertParamProxy paramProxy = InsertParamProxy.build(param);
        String primaryKeyName = getPrimaryKeyName(name);
        String pk = GenericPo.createUID();
        paramProxy.value(primaryKeyName, pk);
        insert.insert(paramProxy);
        return pk;
    }

    @Override
    public String saveOrUpdate(String name, Map<String, Object> data) throws Exception {
        String id = getRepeatDataId(name, data);
        if (id != null) {
            update(name, new UpdateMapParam(data).where(getPrimaryKeyName(name), id));
        } else {
            id = insert(name, new InsertMapParam(data));
        }
        return id;
    }

    protected String getRepeatDataId(String name, Map<String, Object> data) {
        if (dynamicFormDataValidator != null) {
            for (DynamicFormDataValidator validator : dynamicFormDataValidator) {
                String id = validator.getRepeatDataId(name, data);
                if (id != null) {
                    return id;
                }
            }
        }
        return null;
    }

    @Override
    @ReadLock
    @LockName(value = "'form.lock.'+#name", isExpression = true)
    public boolean deleteByPk(String name, String pk) throws Exception {
        String primaryKeyName = getPrimaryKeyName(name);
        Table table = getTableByName(name);
        Delete delete = table.createDelete();
        return delete.delete(DeleteParamProxy.build(new DeleteParam()).where(primaryKeyName, pk)) == 1;
    }

    @Override
    @ReadLock
    @LockName(value = "'form.lock.'+#name", isExpression = true)
    public int delete(String name, DeleteParam where) throws Exception {
        Table table = getTableByName(name);
        Delete delete = table.createDelete();
        return delete.delete(DeleteParamProxy.build(where));
    }

    @Override
    @ReadLock
    @LockName(value = "'form.lock.'+#name", isExpression = true)
    public int updateByPk(String name, String pk, UpdateParam<Map<String, Object>> param) throws Exception {
        Table table = getTableByName(name);
        Update update = table.createUpdate();
        UpdateParamProxy paramProxy = UpdateParamProxy.build(param);
        paramProxy.where(getPrimaryKeyName(name), pk);
        return update.update(paramProxy);
    }

    @Override
    @ReadLock
    @LockName(value = "'form.lock.'+#name", isExpression = true)
    public int update(String name, UpdateParam<Map<String, Object>> param) throws Exception {
        Table table = getTableByName(name);
        Update update = table.createUpdate();
        UpdateParamProxy paramProxy = UpdateParamProxy.build(param);
        return update.update(paramProxy);
    }

    @ReadLock
    @LockName(value = "'form.lock.'+#tableName", isExpression = true)
    public String getPrimaryKeyName(String tableName) throws Exception {
        Table table = getTableByName(tableName);
        return table.getMetaData().attrWrapper("primaryKey", "u_id").toString();
    }

    @Override
    @ReadLock
    @LockName(value = "'form.lock.'+#name", isExpression = true)
    public <T> T selectByPk(String name, Object pk) throws Exception {
        Table table = getTableByName(name);
        Query query = table.createQuery();
        QueryParamProxy proxy = new QueryParamProxy();
        proxy.where(getPrimaryKeyName(name), pk);
        return query.single(proxy);
    }

    @Override
    @ReadLock
    @LockName(value = "'form.lock.'+#name", isExpression = true)
    @Transactional(readOnly = true)
    public void exportExcel(String name, QueryParam param, OutputStream outputStream) throws Exception {
        List<Object> dataList = select(name, param);
        Table table = getTableByName(name);
        TableMetaData metaData = table.getMetaData();
        List<Header> headers = new LinkedList<>();
        Map<String, Object> sample = dataList.isEmpty() ? new HashMap<>() : (Map) dataList.get(0);
        int[] index = new int[1];
        index[0] = 1;
        metaData.getFields().forEach(fieldMetaData -> {
            ValueWrapper valueWrapper = fieldMetaData.attrWrapper("export-excel", false);
            if (valueWrapper.toBoolean()) {
                String title = fieldMetaData.attrWrapper("export-header", fieldMetaData.getComment()).toString();
                if (StringUtils.isNullOrEmpty(title)) {
                    title = "字段" + index[0]++;
                }
                String field = fieldMetaData.getName();
                Set<String> includes = param.getIncludes();
                Set<String> excludes = param.getExcludes();
                if (!includes.isEmpty()) {
                    if (!includes.contains(field)) return;
                }
                if (!excludes.isEmpty()) {
                    if (excludes.contains(field)) return;
                }
                if (sample.get(field + "_cn") != null)
                    field = field + "_cn";
                headers.add(new Header(title, field));
            }
        });
        if (metaData.triggerSupport("export.excel")) {
            Map<String, Object> var = new HashMap<>();
            if (expressionScopeBeanMap != null)
                var.putAll(expressionScopeBeanMap);
            var.put("dataList", dataList);
            var.put("headers", headers);
            metaData.on("export.excel", var);
        }
        ExcelIO.write(outputStream, headers, dataList);
    }

    @Override
    @ReadLock
    @LockName(value = "'form.lock.'+#name", isExpression = true)
    public Map<String, Object> importExcel(String name, InputStream inputStream) throws Exception {
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();
        List<Map<String, Object>> excelData;
        try {
            excelData = ExcelIO.read2Map(inputStream);
        } catch (Exception e) {
            throw new BusinessException("解析excel失败,请确定文件格式正确!", e, 500);
        }
        List<Map<String, Object>> dataList = new LinkedList<>();
        Map<String, String> headerMapper = new HashMap<>();
        Table table = getTableByName(name);
        TableMetaData metaData = table.getMetaData();
        metaData.getFields().forEach(fieldMetaData -> {
            ValueWrapper valueWrapper = fieldMetaData.attrWrapper("importExcel", true);
            if (valueWrapper.toBoolean()) {
                String title = fieldMetaData.attrWrapper("excelHeader", fieldMetaData.getComment()).toString();
                String field = fieldMetaData.getName();
                headerMapper.put(title, field);
            }
        });
        if (metaData.triggerSupport("export.import.before")) {
            Map<String, Object> var = new HashMap<>();
            var.put("headerMapper", headerMapper);
            var.put("excelData", excelData);
            var.put("dataList", dataList);
            if (expressionScopeBeanMap != null)
                var.putAll(expressionScopeBeanMap);
            TriggerResult triggerResult = metaData.on("export.import.before", var);
            if (!triggerResult.isSuccess()) {
                throw new TriggerException(triggerResult.getMessage());
            }
        }
        excelData.forEach(data -> {
            Map<String, Object> newData = new HashMap<>();
            data.forEach((k, v) -> {
                String field = headerMapper.get(k);
                if (field != null) {
                    newData.put(field, v);
                } else {
                    newData.put(k, v);
                }
            });
            dataList.add(newData);
        });
        List<Map<String, Object>> errorMessage = new LinkedList<>();
        int index = 0, success = 0;
        for (Map<String, Object> map : dataList) {
            index++;
            try {
                if (metaData.triggerSupport("export.import.each")) {
                    Map<String, Object> var = new HashMap<>();
                    var.put("headerMapper", headerMapper);
                    var.put("excelData", excelData);
                    var.put("dataList", dataList);
                    var.put("data", map);
                    if (expressionScopeBeanMap != null)
                        var.putAll(expressionScopeBeanMap);
                    TriggerResult triggerResult = metaData.on("export.import.each", var);
                    if (!triggerResult.isSuccess()) {
                        throw new TriggerException(triggerResult.getMessage());
                    }
                }
                saveOrUpdate(name, map);
                success++;
            } catch (Exception e) {
                Map<String, Object> errorMsg = new HashMap<>();
                errorMsg.put("index", index);
                errorMsg.put("message", e.getMessage());
                errorMessage.add(errorMsg);
            }
        }
        long endTime = System.currentTimeMillis();
        result.put("startTime", startTime);
        result.put("endTime", endTime);
        result.put("total", dataList.size());
        result.put("success", success);
        result.put("errorMessage", errorMessage);
        return result;
    }

    public static class QueryParamProxy extends org.webbuilder.sql.param.query.QueryParam {
        public QueryParamProxy orderBy(String mode, Set<String> fields) {
            addProperty("order_by", fields);
            addProperty("order_by_mod", mode);
            return this;
        }

        public static QueryParamProxy build(QueryParam param) {
            QueryParamProxy proxy = new QueryParamProxy();
            proxy.setConditions(term2cdt(param.getTerms()));
            proxy.exclude(param.getExcludes());
            proxy.include(param.getIncludes());
            proxy.orderBy(param.getSortOrder(), param.getSortField());
            proxy.doPaging(param.getPageIndex(), param.getPageSize());
            proxy.setPaging(param.isPaging());
            return proxy;
        }
    }

    public static class UpdateParamProxy extends org.webbuilder.sql.param.update.UpdateParam {
        public static UpdateParamProxy build(UpdateParam<Map<String, Object>> param) {
            UpdateParamProxy proxy = new UpdateParamProxy();
            proxy.setConditions(term2cdt(param.getTerms()));
            proxy.exclude(param.getExcludes());
            proxy.include(param.getIncludes());
            proxy.set(param.getData());
            return proxy;
        }
    }

    public static class InsertParamProxy extends org.webbuilder.sql.param.insert.InsertParam {
        public static InsertParamProxy build(InsertParam<Map<String, Object>> param) {
            InsertParamProxy proxy = new InsertParamProxy();
            proxy.values(param.getData());
            return proxy;
        }
    }

    public static class DeleteParamProxy extends org.webbuilder.sql.param.delete.DeleteParam {
        public static DeleteParamProxy build(DeleteParam param) {
            DeleteParamProxy proxy = new DeleteParamProxy();
            proxy.setConditions(term2cdt(param.getTerms()));
            return proxy;
        }
    }

    protected static Set<ExecuteCondition> term2cdt(List<Term> terms) {
        Set<ExecuteCondition> set = new LinkedHashSet<>();
        terms.forEach(term -> {
            ExecuteCondition executeCondition = new ExecuteCondition();
            executeCondition.setAppendType(term.getType().toString());
            executeCondition.setField(term.getField());
            executeCondition.setValue(term.getValue());
            executeCondition.setQueryType(term.getTermType().toString().toUpperCase());
            executeCondition.setSql(false);
            if (!term.getTerms().isEmpty())
                executeCondition.setNest(term2cdt(term.getTerms()));
            set.add(executeCondition);
        });
        return set;
    }
}
