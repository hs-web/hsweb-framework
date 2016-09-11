package org.hsweb.web.service.impl.module;

import org.hsweb.web.bean.po.module.ModuleMeta;
import org.hsweb.web.core.utils.RandomUtil;
import org.hsweb.web.dao.module.ModuleMetaMapper;
import org.hsweb.web.service.impl.AbstractServiceImpl;
import org.hsweb.web.service.module.ModuleMetaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

/**
 * Created by zhouhao on 16-5-10.
 */
@Service("moduleMetaService")
public class ModuleMetaServiceImpl extends AbstractServiceImpl<ModuleMeta, String> implements ModuleMetaService {

    public static final String CACHE_NAME ="module.meta";

    @Autowired
    private ModuleMetaMapper moduleMetaMapper;

    @Override
    protected ModuleMetaMapper getMapper() {
        return moduleMetaMapper;
    }

    @Override
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public int update(ModuleMeta data)  {
        return super.update(data);
    }

}
