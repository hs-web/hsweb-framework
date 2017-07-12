package org.hswebframework.web.service.form.simple;

import org.hsweb.ezorm.core.Delete;
import org.hsweb.ezorm.core.Update;
import org.hsweb.ezorm.rdb.RDBDatabase;
import org.hsweb.ezorm.rdb.RDBQuery;
import org.hsweb.ezorm.rdb.RDBTable;
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

import java.sql.SQLException;
import java.util.List;

@Service("dynamicFormOperationService")
@Transactional
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

    protected <T> RDBTable<T> getTable(String formId){
        DynamicFormEntity entity= dynamicFormService.selectByPk(formId);
        if(null==entity)throw new NotFoundException("表单不存在");

        RDBDatabase database=entity.getDataSourceId()==null?databaseRepository.getDatabase(entity.getDataSourceId()):
                databaseRepository.getDefaultDatabase();
        return database.getTable(entity.getTableName());
    };
    @Override
    public <T> PagerResult<T> selectPager(String formId, QueryParamEntity paramEntity) {
        RDBTable<T> table=getTable(formId);
        try {
            RDBQuery<T> query=table.createQuery();

            int total= query.setParam(paramEntity).total();
            if(total==0){
                return PagerResult.empty();
            }
            paramEntity.rePaging(total);
            List<T> list =query.setParam(paramEntity).list();
            return PagerResult.of(total,list);
        } catch (SQLException e) {
            //todo custom exception
            throw new RuntimeException(e);
        }

    }

    @Override
    public <T> List<T> select(String formId, QueryParamEntity paramEntity) {
        RDBTable<T> table=getTable(formId);
        try {
            RDBQuery<T> query=table.createQuery();
            return query.setParam(paramEntity).list();
        } catch (SQLException e) {
            //todo custom exception
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T selectSingle(String formId, QueryParamEntity paramEntity) {
        RDBTable<T> table=getTable(formId);
        try {
            RDBQuery<T> query=table.createQuery();

            return query.setParam(paramEntity).single();
        } catch (SQLException e) {
            //todo custom exception
            throw new RuntimeException(e);
        }
    }

    @Override
    public int count(String formId, QueryParamEntity paramEntity) {
        RDBTable table=getTable(formId);
        try {
            RDBQuery query=table.createQuery();

            return query.setParam(paramEntity).total();
        } catch (SQLException e) {
            //todo custom exception
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> int update(String formId, UpdateParamEntity<T> paramEntity) {
        RDBTable table=getTable(formId);
        try {
            Update<T> update=table.createUpdate();

            return update.setParam(paramEntity).exec();
        } catch (SQLException e) {
            //todo custom exception
            throw new RuntimeException(e);
        }
    }

    @Override
    public int delete(String formId, DeleteParamEntity paramEntity) {
        RDBTable table=getTable(formId);
        try {
            Delete delete=table.createDelete();

            return delete.setParam(paramEntity).exec();
        } catch (SQLException e) {
            //todo custom exception
            throw new RuntimeException(e);
        }
    }
}
