package org.hswebframework.web.service.form.simple;

import org.hswebframework.ezorm.rdb.mapping.SyncRepository;
import org.hswebframework.ezorm.rdb.mapping.defaults.record.Record;
import org.hswebframework.web.NotFoundException;
import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.commons.entity.param.DeleteParamEntity;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.form.DynamicFormOperationService;
import org.hswebframework.web.service.form.DynamicFormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DefaultDynamicFormOperationService implements DynamicFormOperationService {

    @Autowired
    private DynamicFormService formService;

    private SyncRepository<Record, String> getRepository(String formId) {
        return Optional.ofNullable(formService.getRepository(formId))
                .orElseThrow(() -> new NotFoundException("表单[" + formId + "]不存在"));

    }

    @Override
    public PagerResult<Record> selectPager(String formId, QueryParamEntity paramEntity) {
        SyncRepository<Record, String> repository = getRepository(formId);
        int count = repository.createQuery()
                .setParam(paramEntity)
                .count();
        if (count == 0) {
            return PagerResult.empty();
        }

        paramEntity.rePaging(count);
        return PagerResult.of(count, repository.createQuery()
                .setParam(paramEntity)
                .fetch(), paramEntity);
    }

    @Override
    public Record selectSingle(String formId, QueryParamEntity paramEntity) {
        return getRepository(formId).createQuery().setParam(paramEntity).fetchOne().orElse(null);
    }

    @Override
    public List<Record> select(String formId, QueryParamEntity paramEntity) {
        return getRepository(formId).createQuery().setParam(paramEntity).fetch();
    }

    @Override
    public int count(String formId, QueryParamEntity paramEntity) {
        return getRepository(formId).createQuery().setParam(paramEntity).count();
    }

    @Override
    public Record updateById(String formId, String id, Map<String, Object> data) {
        Record old = selectById(formId, id);
        int len = getRepository(formId)
                .createUpdate()
                .set(Record.newRecord(data))
                .where("id", id)
                .execute();
        if (len == 0) {
            return null;
        }

        return old;
    }

    @Override
    public Record insert(String formId, Map<String, Object> entity) {
        Record record = Record.newRecord(entity);
        if (!record.get("id").isPresent()) {
            record.putValue("id", IDGenerator.MD5.generate());
        }
        getRepository(formId)
                .insert(record);
        return record;
    }

    @Override
    public int delete(String formId, DeleteParamEntity paramEntity) {
//        return getRepository(formId).createDelete();
        throw new UnsupportedOperationException();
    }

    @Override
    public int deleteById(String formId, String id) {
        return getRepository(formId)
                .deleteById(id);
    }

    @Override
    public Record saveOrUpdate(String formId, Map<String, Object> data) {
        String id = String.valueOf(data.get("id"));
        if (id != null) {
            return updateById(formId, id, data);
        }
        return insert(formId, data);
    }

    @Override
    public Record selectById(String formId, String id) {
        return getRepository(formId).findById(id).orElse(null);
    }


}
