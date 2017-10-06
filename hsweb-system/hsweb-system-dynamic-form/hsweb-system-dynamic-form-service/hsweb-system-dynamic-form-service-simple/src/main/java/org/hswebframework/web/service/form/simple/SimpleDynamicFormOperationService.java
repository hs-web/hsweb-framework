package org.hswebframework.web.service.form.simple;

import org.hswebframework.ezorm.core.Delete;
import org.hswebframework.ezorm.core.Insert;
import org.hswebframework.ezorm.core.Update;
import org.hswebframework.ezorm.rdb.RDBDatabase;
import org.hswebframework.ezorm.rdb.RDBQuery;
import org.hswebframework.ezorm.rdb.RDBTable;
import org.hswebframework.web.NotFoundException;
import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.commons.entity.param.DeleteParamEntity;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.commons.entity.param.UpdateParamEntity;
import org.hswebframework.web.entity.form.DynamicFormEntity;
import org.hswebframework.web.service.form.DatabaseRepository;
import org.hswebframework.web.service.form.DynamicFormOperationService;
import org.hswebframework.web.service.form.DynamicFormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@Service("dynamicFormOperationService")
@Transactional(rollbackFor = Throwable.class)
public class SimpleDynamicFormOperationService implements DynamicFormOperationService {

    private DynamicFormService dynamicFormService;

    private DatabaseRepository databaseRepository;

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
        if (null == form) throw new NotFoundException("表单不存在");

        RDBDatabase database = StringUtils.isEmpty(form.getDataSourceId()) ?
                databaseRepository.getDefaultDatabase() : databaseRepository.getDatabase(form.getDataSourceId());
        return database.getTable(form.getDatabaseTableName());
    }

    @Override
    @Transactional(readOnly = true)
    public <T> PagerResult<T> selectPager(String formId, QueryParamEntity paramEntity) {
        RDBTable<T> table = getTable(formId);
        try {
            RDBQuery<T> query = table.createQuery();
            int total = query.setParam(paramEntity).total();
            if (total == 0) {
                return PagerResult.empty();
            }
            paramEntity.rePaging(total);
            List<T> list = query.setParam(paramEntity).list();
            return PagerResult.of(total, list);
        } catch (SQLException e) {
            throw new DynamicFormException("selectPager fail", e);
        }

    }

    @Override
    @Transactional(readOnly = true)
    public <T> List<T> select(String formId, QueryParamEntity paramEntity) {
        RDBTable<T> table = getTable(formId);
        try {
            RDBQuery<T> query = table.createQuery();
            return query.setParam(paramEntity).list();
        } catch (SQLException e) {
            throw new DynamicFormException("select fail", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public <T> T selectSingle(String formId, QueryParamEntity paramEntity) {
        RDBTable<T> table = getTable(formId);
        try {
            RDBQuery<T> query = table.createQuery();

            return query.setParam(paramEntity).single();
        } catch (SQLException e) {
            throw new DynamicFormException("selectSingle fail", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public int count(String formId, QueryParamEntity paramEntity) {
        RDBTable table = getTable(formId);
        try {
            RDBQuery query = table.createQuery();

            return query.setParam(paramEntity).total();
        } catch (SQLException e) {
            throw new DynamicFormException("count fail", e);
        }
    }

    @Override
    public <T> int update(String formId, UpdateParamEntity<T> paramEntity) {
        if (Objects.requireNonNull(paramEntity).getTerms().isEmpty()) {
            throw new UnsupportedOperationException("can not use empty condition for update");
        }
        RDBTable<T> table = getTable(formId);
        try {
            Update<T> update = table.createUpdate();

            return update.setParam(paramEntity).exec();
        } catch (SQLException e) {
            throw new DynamicFormException("update fail", e);
        }
    }

    @Override
    public <T> void insert(String formId, T entity) {
        RDBTable<T> table = getTable(formId);
        try {
            Insert<T> insert = table.createInsert();
            insert.value(entity).exec();
        } catch (SQLException e) {
            throw new DynamicFormException("insert fail", e);
        }
    }

    @Override
    public int delete(String formId, DeleteParamEntity paramEntity) {
        if (Objects.requireNonNull(paramEntity).getTerms().isEmpty()) {
            throw new UnsupportedOperationException("can not use empty condition for delete");
        }
        RDBTable table = getTable(formId);
        try {
            Delete delete = table.createDelete();

            return delete.setParam(paramEntity).exec();
        } catch (SQLException e) {
            throw new DynamicFormException("delete fail", e);
        }
    }
}
