package org.hswebframework.web.service.organizational.simple;

import org.hswebframework.web.dao.organizational.RelationDefineDao;
import org.hswebframework.web.entity.organizational.RelationDefineEntity;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.organizational.RelationDefineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("relationDefineService")
public class SimpleRelationDefineService extends GenericEntityService<RelationDefineEntity, String>
        implements RelationDefineService {
    @Autowired
    private RelationDefineDao relationDefineDao;
   @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }
    @Override
    public RelationDefineDao getDao() {
        return relationDefineDao;
    }

}
