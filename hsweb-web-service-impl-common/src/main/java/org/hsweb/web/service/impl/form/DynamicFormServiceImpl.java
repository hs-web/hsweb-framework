package org.hsweb.web.service.impl.form;

import org.hsweb.web.bean.common.PagerResult;
import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.bean.po.form.Form;
import org.hsweb.web.service.form.DynamicFormService;
import org.hsweb.web.service.form.FormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.webbuilder.sql.DataBase;
import org.webbuilder.sql.TableMetaData;

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

    @Override
    public void deploy(Form form) throws Exception {
        Lock writeLock = lock.writeLock();
        try {
            writeLock.lock();
            TableMetaData metaData = formParser.parse(form);
            List<Form> oldList = formService.select(new QueryParam().where("name", form.getName()).where("version", form.getVersion() - 1));
            Form old = null;
            if (oldList.size() > 0) old = oldList.get(0);
            if (old != null)
                dataBase.updateTable(metaData);
            else
                dataBase.createTable(metaData);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void unDeploy(Form form) throws Exception {
        Lock writeLock = lock.writeLock();
        try {
            writeLock.lock();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public <T> PagerResult<T> selectPager(String name, QueryParam param) throws Exception {
        Lock readLock = lock.readLock();
        try {
            readLock.lock();
            System.out.println("执行");
            Thread.sleep(1000);
        } finally {
            readLock.unlock();
        }
        return null;
    }

    @Override
    public <T> List<T> select(String name, QueryParam param) throws Exception {
        return null;
    }

    @Override
    public int total(String name, QueryParam param) throws Exception {
        return 0;
    }

    @Override
    public int insert(String name, Map<String, Object> data) throws Exception {
        return 0;
    }

    @Override
    public int delete(String name, Map<String, Object> data) throws Exception {
        return 0;
    }

    @Override
    public int update(String name, Map<String, Object> data) throws Exception {
        return 0;
    }

    @Override
    public <T> T selectByPk(String name, Object pk) throws Exception {
        return null;
    }


}
