package org.hsweb.web.service.impl.template;

import com.alibaba.fastjson.JSON;
import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.bean.common.UpdateParam;
import org.hsweb.web.bean.po.history.History;
import org.hsweb.web.bean.po.template.Template;
import org.hsweb.web.dao.GenericMapper;
import org.hsweb.web.dao.template.TemplateMapper;
import org.hsweb.web.service.history.HistoryService;
import org.hsweb.web.service.impl.AbstractServiceImpl;
import org.hsweb.web.service.template.TemplateService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * Created by zhouhao on 16-5-20.
 */
@Service("templateService")
public class TemplateServiceImpl extends AbstractServiceImpl<Template, String> implements TemplateService {

    private static final String CACHE_NAME = "template";

    @Resource
    private TemplateMapper templateMapper;

    @Resource
    private HistoryService historyService;

    @Override
    protected TemplateMapper getMapper() {
        return templateMapper;
    }

    @Override
    public String insert(Template data)  {
        data.setVersion(1);
        data.setUsing(false);
        data.setRelease(0);
        data.setRevision(0);
        return super.insert(data);
    }

    @Override
    public String createNewVersion(String oldVersionId) {
        Template old = templateMapper.selectByPk(oldVersionId);
        assertNotNull(old, "模板不存在");
        old.setId(null);
        old.setVersion(old.getVersion() + 1);
        old.setRevision(0);
        old.setRelease(0);
        old.setUsing(false);
        insert(old);
        return old.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Template> selectLatestList(QueryParam param) {
        return templateMapper.selectLatestList(param);
    }

    @Override
    @Transactional(readOnly = true)
    public int countLatestList(QueryParam param) {
        return templateMapper.countLatestList(param);
    }

    @Override
    @CacheEvict(value = CACHE_NAME,
            key = "'template.name.using'+target.selectByPk(#id).getName()",
            condition = "target.selectByPk(#id).isUsing()")
    public int update(Template data) {
        Template old = selectByPk(data.getId());
        assertNotNull(old, "模板不存在");
        data.setRevision(old.getRevision() + 1);
        UpdateParam<Template> param = new UpdateParam<>(data)
                .excludes("version", "revision", "release", "using")
                .where("id", data.getId());
        return templateMapper.update(param);
    }

    @Override
    @Caching(
            evict = {
                    @CacheEvict(value = CACHE_NAME, key = "'template.using.name.'+target.selectByPk(#id).getName()"),
                    @CacheEvict(value = CACHE_NAME, key = "'template.deploy.name.'+target.selectByPk(#id).getName()")
            }
    )
    public void deploy(String id) {
        Template old = templateMapper.selectByPk(id);
        assertNotNull(old, "模板不存在");
        Template usingTemplate = selectUsing(old.getName());
        if (usingTemplate != null) {
            usingTemplate.setUsing(true);
            templateMapper.update(new UpdateParam<>(usingTemplate).includes("using").where("id", usingTemplate.getId()));
        }
        old.setUsing(true);
        templateMapper.update(new UpdateParam<>(old).includes("using").where("id", old.getId()));
        History history = new History();
        history.setPrimaryKeyName("id");
        history.setPrimaryKeyValue(id);
        history.setChangeAfter(JSON.toJSONString(old));
        history.setDescribe("模板发布历史");
        history.setType("template.deploy." + old.getName());
        history.setCreateDate(new Date());
        historyService.insert(history);
    }

    @Override
    @Caching(
            evict = {
                    @CacheEvict(value = CACHE_NAME, key = "'template.using.name.'+target.selectByPk(#id).getName()")
                    , @CacheEvict(value = CACHE_NAME, key = "'template.deploy.name'+target.selectByPk(#id).getName()")
            }
    )
    public void unDeploy(String id) {
        Template old = templateMapper.selectByPk(id);
        assertNotNull(old, "模板不存在");
        old.setUsing(false);
        templateMapper.update(new UpdateParam<>(old).includes("using").where("id", old.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public Template selectLatest(String name) {
        QueryParam param = QueryParam.build()
                .where("name", name).orderBy("version").desc().doPaging(0, 1);
        List<Template> templates = selectLatestList(param);
        return templates.size() > 0 ? templates.get(0) : null;
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "'template.name.'+#name+':'+#version")
    public Template selectByVersion(String name, int version) {
        QueryParam param = QueryParam.build().where("name", name).and("version", version);
        return this.selectSingle(param);
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "'template.deploy.name.'+#name")
    public Template selectDeploy(String name) {
        Template deployed = selectUsing(name);
        assertNotNull(deployed, "模板不存在或未部署");
        History history = historyService.selectLastHistoryByType("template.deploy." + name);
        assertNotNull(history, "模板不存在或未部署");
        return JSON.parseObject(history.getChangeAfter(), Template.class);
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "'template.using.name.'+#name")
    public Template selectUsing(String name) {
        QueryParam param = QueryParam.build().where("name", name).and("using", true);
        return this.selectSingle(param);
    }
}
