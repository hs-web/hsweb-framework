package org.hswebframework.web.service.template.simple;

import org.hswebframework.web.dao.template.TemplateDao;
import org.hswebframework.web.entity.template.TemplateEntity;
import org.hswebframework.web.service.EnableCacheGenericEntityService;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.template.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import java.util.regex.Pattern;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("templateService")
@CacheConfig(cacheNames = "template")
public class SimpleTemplateService extends EnableCacheGenericEntityService<TemplateEntity, String>
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

    @Override
    public String insert(TemplateEntity entity) {
        entity.setVersion(1L);
        return super.insert(entity);
    }
}
