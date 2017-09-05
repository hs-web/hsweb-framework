package org.hswebframework.web.service.workflow.simple;

import org.hswebframework.web.dao.CrudDao;
import org.hswebframework.web.dao.workflow.ActDefDao;
import org.hswebframework.web.entity.workflow.ActDefEntity;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.service.workflow.ActDefService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author wangwei
 * @Date 2017/9/5.
 */
@Service
public class SimpleActDefService extends GenericEntityService<ActDefEntity,String> implements ActDefService {

    @Autowired
    private ActDefDao actDefDao;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public CrudDao<ActDefEntity, String> getDao() {
        return actDefDao;
    }
}
