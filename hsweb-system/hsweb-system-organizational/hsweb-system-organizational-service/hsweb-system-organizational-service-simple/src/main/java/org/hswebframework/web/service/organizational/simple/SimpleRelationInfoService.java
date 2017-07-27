package org.hswebframework.web.service.organizational.simple;

import org.hswebframework.web.dao.organizational.RelationInfoDao;
import org.hswebframework.web.entity.organizational.RelationInfoEntity;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.organizational.RelationInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("relationInfoService")
public class SimpleRelationInfoService extends GenericEntityService<RelationInfoEntity, String>
        implements RelationInfoService {
    @Autowired
    private RelationInfoDao relationInfoDao;
   @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }
    @Override
    public RelationInfoDao getDao() {
        return relationInfoDao;
    }

}
