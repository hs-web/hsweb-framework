package org.hswebframework.web.workflow.service.imp;

import org.hswebframework.web.dao.CrudDao;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.workflow.dao.ProcessHistoryDao;
import org.hswebframework.web.workflow.dao.entity.ProcessHistoryEntity;
import org.hswebframework.web.workflow.service.ProcessHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@Component
public class ProcessHistoryServiceImpl extends GenericEntityService<ProcessHistoryEntity, String> implements ProcessHistoryService {

    @Autowired
    private ProcessHistoryDao processHistoryDao;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public CrudDao<ProcessHistoryEntity, String> getDao() {
        return processHistoryDao;
    }

    @Override
    public String insert(ProcessHistoryEntity entity) {
        entity.setCreateTime(new Date());

        return super.insert(entity);
    }
}
