package org.hsweb.web.service.impl.module;

import org.hsweb.ezorm.param.Term;
import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.bean.po.module.ModuleMeta;
import org.hsweb.web.core.utils.RandomUtil;
import org.hsweb.web.dao.module.ModuleMetaMapper;
import org.hsweb.web.service.impl.AbstractServiceImpl;
import org.hsweb.web.service.module.ModuleMetaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Created by zhouhao on 16-5-10.
 */
@Service("moduleMetaService")
public class ModuleMetaServiceImpl extends AbstractServiceImpl<ModuleMeta, String> implements ModuleMetaService {

    public static final String CACHE_NAME = "module.meta";

    @Autowired
    private ModuleMetaMapper moduleMetaMapper;

    @Override
    protected ModuleMetaMapper getMapper() {
        return moduleMetaMapper;
    }

    @Override
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public int update(ModuleMeta data) {
        return super.update(data);
    }

    @Override
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public String insert(ModuleMeta data) {
        return super.insert(data);
    }

    @Override
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public int delete(String s) {
        return super.delete(s);
    }

    protected QueryParam createSelectByKeyAndRoleIdParam(String key, List<String> roleId) {
        QueryParam param = QueryParam.build();
        param.nest().and("key", key).or("moduleId", key).or("id",key);
        Term term = param.nest();
        roleId.forEach(id -> term.or("roleId$LIKE", "%," + id + ",%"));
        term.or("roleId$ISNULL", true).or("roleId$EMPTY", true);
        return param;
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "'list.key:'+#key+'-roleId:'+#roleId.hashCode()")
    public List<ModuleMeta> selectByKeyAndRoleId(String key, List<String> roleId) {
        return this.select(createSelectByKeyAndRoleIdParam(key, roleId));
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "'single.key:'+#key+'-roleId:'+#roleId.hashCode()")
    public ModuleMeta selectSingleByKeyAndRoleId(String key, List<String> roleId) {
        return this.selectSingle(createSelectByKeyAndRoleIdParam(key, roleId));
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "'single.key:'+#key+'-roleId:'+#roleId.hashCode()+'.md5'")
    public String selectMD5SingleByKeyAndRoleId(String key, List<String> roleId) {
        ModuleMeta meta = this.selectSingle(createSelectByKeyAndRoleIdParam(key, roleId));
        assertNotNull(meta, "数据不存在");
        return meta.getMd5();
    }
}
