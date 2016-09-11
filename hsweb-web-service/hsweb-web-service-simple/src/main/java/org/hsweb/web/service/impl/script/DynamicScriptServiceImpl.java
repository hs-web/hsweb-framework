package org.hsweb.web.service.impl.script;

import org.hsweb.commons.MD5;
import org.hsweb.expands.script.engine.*;
import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.bean.common.UpdateParam;
import org.hsweb.web.bean.po.script.DynamicScript;
import org.hsweb.web.core.authorize.ExpressionScopeBean;
import org.hsweb.web.dao.script.DynamicScriptMapper;
import org.hsweb.web.core.exception.BusinessException;
import org.hsweb.web.service.impl.AbstractServiceImpl;
import org.hsweb.web.service.script.DynamicScriptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 动态脚本服务类
 * Created by generator
 */
@Service("dynamicScriptService")
public class DynamicScriptServiceImpl extends AbstractServiceImpl<DynamicScript, String> implements DynamicScriptService {

    private static final String CACHE_KEY = "dynamic_script";

    //默认数据映射接口
    @Resource
    protected DynamicScriptMapper dynamicScriptMapper;

    @Override
    protected DynamicScriptMapper getMapper() {
        return this.dynamicScriptMapper;
    }

    @Override
    public String insert(DynamicScript data) {
        DynamicScript old = selectSingle(QueryParam.build().where("name", data.getName()).and("type", data.getType()));
        if (old != null) throw new BusinessException("已存在相同名称和类型的脚本!", 400);
        data.setStatus(1);
        return super.insert(data);
    }

    @Override
    @Cacheable(value = CACHE_KEY, key = "'script.'+#pk")
    public DynamicScript selectByPk(String pk) {
        return super.selectByPk(pk);
    }

    @Override
    @Cacheable(value = CACHE_KEY, key = "'script.md5.'+#pk")
    public String getScriptMd5(String scriptId) {
        DynamicScript script = selectByPk(scriptId);
        assertNotNull(script, "脚本不存在");
        return MD5.defaultEncode(script.getContent());
    }

    @Override
    @Cacheable(value = CACHE_KEY, key = "'script.'+#name+'.'+#type")
    public DynamicScript selectByNameAndType(String name, String type) throws Exception {
        return selectSingle(QueryParam.build().where("name", name).and("type", type));
    }

    @Override
    @CacheEvict(value = CACHE_KEY, allEntries = true)
    public int update(DynamicScript data) {
        DynamicScript old = selectSingle(QueryParam.build()
                .where("name", data.getName())
                .and("type", data.getType())
                .and("id$not", data.getId()));
        if (old != null) throw new BusinessException("已存在相同名称和类型的脚本!", 400);
        int i = getMapper().update(UpdateParam.build(data).excludes("status").where("id", data.getId()));
        return i;
    }

    @Override
    @CacheEvict(value = CACHE_KEY, allEntries = true)
    public int update(List<DynamicScript> datas) {
        return super.update(datas);
    }

    @Override
    @CacheEvict(value = CACHE_KEY, allEntries = true)
    public int delete(String pk) {
        return super.delete(pk);
    }

    public void compile(String id) throws Exception {
        DynamicScript script = this.selectByPk(id);
        assertNotNull(script, "脚本不存在");
        DynamicScriptEngine engine = DynamicScriptEngineFactory.getEngine(script.getType());
        assertNotNull(engine, "不支持的引擎");
        engine.compile(script.getId(), script.getContent());
    }

    public void compileAll() throws Exception {
        List<DynamicScript> list = this.select(QueryParam.build().where("status", 1).noPaging());
        for (DynamicScript script : list) {
            DynamicScriptEngine engine = DynamicScriptEngineFactory.getEngine(script.getType());
            assertNotNull(engine, "不支持的引擎");
            engine.compile(script.getId(), script.getContent());
        }
    }

}
