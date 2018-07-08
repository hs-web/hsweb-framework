package org.hswebframework.web.service.form.simple;

import lombok.SneakyThrows;
import org.hswebframework.ezorm.core.Delete;
import org.hswebframework.ezorm.core.Insert;
import org.hswebframework.ezorm.core.Update;
import org.hswebframework.ezorm.rdb.RDBDatabase;
import org.hswebframework.ezorm.rdb.RDBQuery;
import org.hswebframework.ezorm.rdb.RDBTable;
import org.hswebframework.web.NotFoundException;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.commons.entity.param.DeleteParamEntity;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.commons.entity.param.UpdateParamEntity;
import org.hswebframework.web.entity.form.DynamicFormEntity;
import org.hswebframework.web.service.form.DatabaseRepository;
import org.hswebframework.web.service.form.DynamicFormOperationService;
import org.hswebframework.web.service.form.DynamicFormService;
import org.hswebframework.web.service.form.events.FormDataInsertBeforeEvent;
import org.hswebframework.web.service.form.events.FormDataUpdateBeforeEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service("dynamicFormOperationService")
@Transactional(rollbackFor = Throwable.class)
public class SimpleDynamicFormOperationService implements DynamicFormOperationService {

    private DynamicFormService dynamicFormService;

    private DatabaseRepository databaseRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    public void setDynamicFormService(DynamicFormService dynamicFormService) {
        this.dynamicFormService = dynamicFormService;
    }

    @Autowired
    public void setDatabaseRepository(DatabaseRepository databaseRepository) {
        this.databaseRepository = databaseRepository;
    }

    protected <T> RDBTable<T> getTable(String formId) {
        DynamicFormEntity form = dynamicFormService.selectByPk(formId);
        if (null == form || Boolean.FALSE.equals(form.getDeployed())) {
            throw new NotFoundException("表单不存在");
        }
        RDBDatabase database = StringUtils.isEmpty(form.getDataSourceId()) ?
                databaseRepository.getDefaultDatabase() : databaseRepository.getDatabase(form.getDataSourceId());
        return database.getTable(form.getDatabaseTableName());
    }

    @Override
    @Transactional(readOnly = true)
    @SneakyThrows
    public <T> PagerResult<T> selectPager(String formId, QueryParamEntity paramEntity) {
        RDBTable<T> table = getTable(formId);
        RDBQuery<T> query = table.createQuery();
        int total = query.setParam(paramEntity).total();
        if (total == 0) {
            return PagerResult.empty();
        }
        paramEntity.rePaging(total);
        List<T> list = query.setParam(paramEntity).list(paramEntity.getPageIndex(), paramEntity.getPageSize());
        return PagerResult.of(total, list);
    }

    @Override
    @Transactional(readOnly = true)
    @SneakyThrows
    public <T> List<T> select(String formId, QueryParamEntity paramEntity) {
        RDBTable<T> table = getTable(formId);
        RDBQuery<T> query = table.createQuery();
        return query.setParam(paramEntity).list();
    }

    @Override
    @Transactional(readOnly = true)
    @SneakyThrows
    public <T> T selectSingle(String formId, QueryParamEntity paramEntity) {
        RDBTable<T> table = getTable(formId);
        RDBQuery<T> query = table.createQuery();
        return query.setParam(paramEntity).single();
    }

    @Override
    @Transactional(readOnly = true)
    @SneakyThrows
    public int count(String formId, QueryParamEntity paramEntity) {
        RDBTable table = getTable(formId);
        RDBQuery query = table.createQuery();
        return query.setParam(paramEntity).total();
    }

    @Override
    @SneakyThrows
    public <T> int update(String formId, UpdateParamEntity<T> paramEntity) {
        if (Objects.requireNonNull(paramEntity).getTerms().isEmpty()) {
            throw new UnsupportedOperationException("不能执行无条件的更新操作");
        }
        RDBTable<T> table = getTable(formId);
        Update<T> update = table.createUpdate();
        return update.setParam(paramEntity).exec();
    }

    @Override
    @SneakyThrows
    public <T> T insert(String formId, T entity) {
        RDBTable<T> table = getTable(formId);
        Insert<T> insert = table.createInsert();
        eventPublisher.publishEvent(new FormDataInsertBeforeEvent<>(formId, table, entity));
        insert.value(entity).exec();
        return entity;
    }

    @Override
    @SneakyThrows
    public <T> T saveOrUpdate(String formId, T entity) {

        Map<String, Object> map = FastBeanCopier.copy(entity, new HashMap<>(), FastBeanCopier.include(idProperty));

        Object id = map.get(idProperty);
        if (id == null) {
            return insert(formId, entity);
        }
        int total = getTable(formId).createQuery().where(idProperty, id).total();
        if (total > 0) {
            return updateById(formId, String.valueOf(id), entity);
        }

        return insert(formId, entity);
    }

    @Override
    @SneakyThrows
    public int delete(String formId, DeleteParamEntity paramEntity) {
        if (Objects.requireNonNull(paramEntity).getTerms().isEmpty()) {
            throw new UnsupportedOperationException("不能执行无条件的删除操作");
        }
        RDBTable table = getTable(formId);
        Delete delete = table.createDelete();
        return delete.setParam(paramEntity).exec();
    }

    @Override
    @SneakyThrows
    public int deleteById(String formId, Object id) {
        Objects.requireNonNull(id, "主键不能为空");
        RDBTable table = getTable(formId);
        return table.createDelete().where(idProperty, id).exec();
    }

    @Override
    @SneakyThrows
    public <T> T selectById(String formId, Object id) {
        Objects.requireNonNull(id, "主键不能为空");
        RDBTable<T> table = getTable(formId);
        return table.createQuery().where(idProperty,id).single();
    }

    @Override
    @SneakyThrows
    public <T> T updateById(String formId, Object id, T data) {
        Objects.requireNonNull(id, "主键不能为空");
        RDBTable<T> table = getTable(formId);
        eventPublisher.publishEvent(new FormDataUpdateBeforeEvent<>(formId, table, data, id));
        table.createUpdate()
                .set(data)
                .where(idProperty, id)
                .exec();
        return data;
    }
}
