package org.hsweb.web.service.impl.form;

import com.alibaba.fastjson.JSON;
import org.hsweb.web.Install;
import org.hsweb.web.bean.common.*;
import org.hsweb.web.bean.po.GenericPo;
import org.hsweb.web.bean.po.form.Form;
import org.hsweb.web.bean.po.history.History;
import org.hsweb.web.service.form.DynamicFormService;
import org.hsweb.web.service.form.FormService;
import org.hsweb.web.service.history.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.webbuilder.sql.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by zhouhao on 16-4-14.
 */
@Service("dynamicFormService")
public class DynamicFormServiceImpl implements DynamicFormService {
    protected ReadWriteLock lock = new ReentrantReadWriteLock();

    @Autowired(required = false)
    protected FormParser formParser = new CommonFormParser();

    @Autowired
    protected DataBase dataBase;

    @Resource
    protected FormService formService;

    @Resource
    protected HistoryService historyService;

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
        FieldMetaData UID = new FieldMetaData("u_id", String.class, dataType);
        UID.setPrimaryKey(true);
        UID.setNotNull(true);
        UID.setComment("主键");
        metaData.attr("primaryKey", "u_id");
        metaData.addField(UID);

    }

    @Override
    public void deploy(Form form) throws Exception {
        Lock writeLock = lock.writeLock();
        try {
            writeLock.lock();
            TableMetaData metaData = formParser.parse(form);
            initDefaultField(metaData);
            History history = historyService.selectLastHistoryByType("form.deploy." + form.getName());
            //首次部署
            if (history == null) {
                dataBase.createTable(metaData);
            } else {
                Form lastDeploy = JSON.parseObject(history.getChange_after(), Form.class);
                TableMetaData lastDeployMetaData = formParser.parse(lastDeploy);
                initDefaultField(lastDeployMetaData);
                //向上发布
                dataBase.updateTable(lastDeployMetaData);//先放入旧的结构
                //更新结构
                dataBase.alterTable(metaData);
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void unDeploy(Form form) throws Exception {
        Lock writeLock = lock.writeLock();
        try {
            writeLock.lock();
            dataBase.removeTable(form.getName());
        } finally {
            writeLock.unlock();
        }
    }

    public Table getTableByName(String name) throws Exception {
        Table table = dataBase.getTable(name.toUpperCase());
        if (table == null)
            table = dataBase.getTable(name.toLowerCase());
        Assert.notNull(table, "表单[" + name + "]不存在");
        return table;
    }

    @Override
    public <T> PagerResult<T> selectPager(String name, QueryParam param) throws Exception {
        Lock readLock = lock.readLock();
        PagerResult<T> result = new PagerResult<>();
        try {
            readLock.lock();
            Table table = getTableByName(name);
            Query query = table.createQuery();
            QueryParamProxy proxy = QueryParamProxy.build(param);
            int total = query.total(proxy);
            result.setTotal(total);
            param.rePaging(total);
            proxy = QueryParamProxy.build(param);
            result.setData(query.list(proxy));
        } finally {
            readLock.unlock();
        }
        return result;
    }

    @Override
    public <T> List<T> select(String name, QueryParam param) throws Exception {
        Lock readLock = lock.readLock();
        try {
            readLock.lock();
            Table table = getTableByName(name);
            Query query = table.createQuery();
            param.setPaging(false);
            QueryParamProxy proxy = QueryParamProxy.build(param);
            return query.list(proxy);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public int total(String name, QueryParam param) throws Exception {
        Lock readLock = lock.readLock();
        try {
            readLock.lock();
            Table table = getTableByName(name);
            Query query = table.createQuery();
            param.setPaging(false);
            QueryParamProxy proxy = QueryParamProxy.build(param);
            return query.total(proxy);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public String insert(String name, InsertParam<Map<String, Object>> param) throws Exception {
        Lock readLock = lock.readLock();
        try {
            readLock.lock();
            Table table = getTableByName(name);
            Insert insert = table.createInsert();
            InsertParamProxy paramProxy = InsertParamProxy.build(param);
            String primaryKeyName = getPrimaryKeyName(name);
            String pk = GenericPo.createUID();
            paramProxy.value(primaryKeyName, pk);
            insert.insert(paramProxy);
            return pk;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean deleteByPk(String name, String pk) throws Exception {
        Lock readLock = lock.readLock();
        try {
            readLock.lock();
            String primaryKeyName = getPrimaryKeyName(name);
            Table table = getTableByName(name);
            Delete delete = table.createDelete();
            return delete.delete(DeleteParamProxy.build(new DeleteParam()).where(primaryKeyName, pk)) == 1;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public int delete(String name, DeleteParam where) throws Exception {
        Lock readLock = lock.readLock();
        try {
            readLock.lock();
            Table table = getTableByName(name);
            Delete delete = table.createDelete();
            return delete.delete(DeleteParamProxy.build(where));
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public int updateByPk(String name, String pk, UpdateParam<Map<String, Object>> param) throws Exception {
        Lock readLock = lock.readLock();
        try {
            readLock.lock();
            Table table = getTableByName(name);
            Update update = table.createUpdate();
            UpdateParamProxy paramProxy = UpdateParamProxy.build(param);
            paramProxy.where(getPrimaryKeyName(name), pk);
            return update.update(paramProxy);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public int update(String name, UpdateParam<Map<String, Object>> param) throws Exception {
        Lock readLock = lock.readLock();
        try {
            readLock.lock();
            Table table = getTableByName(name);
            Update update = table.createUpdate();
            UpdateParamProxy paramProxy = UpdateParamProxy.build(param);
            return update.update(paramProxy);
        } finally {
            readLock.unlock();
        }
    }

    public String getPrimaryKeyName(String tableName) throws Exception {
        Table table = getTableByName(tableName);
        return table.getMetaData().attrWrapper("primaryKey", "u_id").toString();
    }

    @Override
    public <T> T selectByPk(String name, Object pk) throws Exception {
        Lock readLock = lock.readLock();
        try {
            readLock.lock();
            Table table = getTableByName(name);
            Query query = table.createQuery();
            QueryParamProxy proxy = new QueryParamProxy();
            proxy.where(getPrimaryKeyName(name), pk);
            return query.single(proxy);
        } finally {
            readLock.unlock();
        }
    }

    public static class QueryParamProxy extends org.webbuilder.sql.param.query.QueryParam {
        public static QueryParamProxy build(QueryParam param) {
            QueryParamProxy proxy = new QueryParamProxy();
            proxy.where(param.getTerm());
            proxy.exclude(param.getExcludes());
            proxy.include(param.getIncludes());
            proxy.orderBy("desc".equals(param.getSortOrder()), param.getSortField());
            proxy.doPaging(param.getPageIndex(), param.getPageSize());
            proxy.setPaging(param.isPaging());
            return proxy;
        }
    }

    public static class UpdateParamProxy extends org.webbuilder.sql.param.update.UpdateParam {
        public static UpdateParamProxy build(UpdateParam<Map<String, Object>> param) {
            UpdateParamProxy proxy = new UpdateParamProxy();
            proxy.where(param.getTerm());
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
            proxy.where(param.getTerm());
            return proxy;
        }
    }
}
