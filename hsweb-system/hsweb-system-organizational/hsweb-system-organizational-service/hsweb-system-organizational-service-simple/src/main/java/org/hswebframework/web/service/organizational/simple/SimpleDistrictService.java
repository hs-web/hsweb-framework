package org.hswebframework.web.service.organizational.simple;

import org.hswebframework.web.dao.organizational.DistrictDao;
import org.hswebframework.web.entity.organizational.DistrictEntity;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.organizational.DistrictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("districtService")
public class SimpleDistrictService extends GenericEntityService<DistrictEntity, String>
        implements DistrictService {
    @Autowired
    private DistrictDao districtDao;
   @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }
    @Override
    public DistrictDao getDao() {
        return districtDao;
    }

}
