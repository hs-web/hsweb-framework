package org.hsweb.web.service.impl.form;

import com.alibaba.fastjson.JSON;
import org.hsweb.web.bean.common.*;
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

    @Override
    public void deploy(Form form) throws Exception {
        Lock writeLock = lock.writeLock();
        try {
            writeLock.lock();
            TableMetaData metaData = formParser.parse(form);
            History history = historyService.selectLastHistoryByType("form.deploy." + form.getName());
            //首次部署
            if (history == null) {
                dataBase.createTable(metaData);
            } else {
                Form lastDeploy = JSON.parseObject(history.getChange_after(), Form.class);
                TableMetaData lastDeployMetaData = formParser.parse(lastDeploy);
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
    public int insert(String name, InsertParam<Map<String, Object>> param) throws Exception {
        Lock readLock = lock.readLock();
        try {
            readLock.lock();
            Table table = getTableByName(name);
            Insert insert = table.createInsert();
            boolean success = insert.insert(InsertParamProxy.build(param));
            return success ? 1 : 0;
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

    @Override
    public <T> T selectByPk(String name, Object pk) throws Exception {
        Lock readLock = lock.readLock();
        try {
            readLock.lock();
            Table table = getTableByName(name);
            Object pk_name = table.getMetaData().attr("primaryKey");
            if (pk_name == null) {
                pk_name = "u_id";
            }
            Query query = table.createQuery();
            QueryParamProxy proxy = new QueryParamProxy();
            proxy.where(String.valueOf(pk_name), pk);
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
