package org.hsweb.web.service.impl.module;

import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.bean.po.module.ModuleMeta;
import org.hsweb.web.bean.po.module.ModuleMeta.Property;
import org.hsweb.web.dao.module.ModuleMetaMapper;
import org.hsweb.web.service.impl.AbstractServiceImpl;
import org.hsweb.web.service.module.ModuleMetaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;

import static org.hsweb.web.bean.po.module.ModuleMeta.Property.*;

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

    static final Function<Object, Object> roleIdValueMapper = (value) -> "," + value + ",";

    protected QueryParam createSelectByKeyAndRoleIdParam(String key, List<String> roleIds) {
        // (id = ? or key = ? or module_id = ? ) and (role_id like ? or .....) and (role_id is null or role_id ='')
        return createQuery()
                //(id = ? or key = ? or module_id = ? )
                .nest(id, key).or(Property.key, key).or(moduleId, key).end()
                //and ((role_id like ? or .....) or (role_id is null or role_id =''))
                //遍历roleId,使用 like %% 并将值转为 ,value, 格式进行查询
                //如果有条件,应该写sql函数,将数据库中的值转为结果集和参数进行对比
                .nest()
                    .nest().each(roleId, roleIds, query -> query::$like$, roleIdValueMapper).end()
                    .orNest().isNull(roleId).or().isEmpty(roleId).end()
                .end()
                .getParam();
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
