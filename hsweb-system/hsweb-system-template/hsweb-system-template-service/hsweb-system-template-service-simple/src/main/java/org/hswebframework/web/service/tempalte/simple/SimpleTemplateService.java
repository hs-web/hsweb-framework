package org.hswebframework.web.service.tempalte.simple;

import org.hswebframework.web.dao.tempalte.TemplateDao;
import org.hswebframework.web.entity.tempalte.TemplateEntity;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.tempalte.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("templateService")
public class SimpleTemplateService extends GenericEntityService<TemplateEntity, String>
        implements TemplateService {
    @Autowired
    private TemplateDao templateDao;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public TemplateDao getDao() {
        return templateDao;
    }

    @Override
    public int updateByPk(String id, TemplateEntity entity) {
        TemplateEntity old = selectByPk(id);
        assertNotNull(old);
        entity.setVersion(old.getVersion() + 1);
        return super.updateByPk(id, entity);
    }


}
