package org.hsweb.web.service.impl.form;

import com.alibaba.fastjson.JSON;
import org.hsweb.web.bean.common.InsertParam;
import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.bean.common.UpdateParam;
import org.hsweb.web.bean.po.form.Form;
import org.hsweb.web.bean.po.history.History;
import org.hsweb.web.dao.form.FormMapper;
import org.hsweb.web.service.form.DynamicFormService;
import org.hsweb.web.service.form.FormParser;
import org.hsweb.web.service.form.FormService;
import org.hsweb.web.service.history.HistoryService;
import org.hsweb.web.service.impl.AbstractServiceImpl;
import org.hsweb.web.core.utils.RandomUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.hsweb.commons.StringUtils;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * 自定义表单服务类
 * Created by generator
 */
@Service("formService")
public class FormServiceImpl extends AbstractServiceImpl<Form, String> implements FormService {

    private static final String CACHE_KEY = "form";

    @Autowired(required = false)
    protected FormParser formParser = new DefaultFormParser();

    @Resource
    private FormMapper formMapper;

    @Resource
    private HistoryService historyService;

    @Override
    protected FormMapper getMapper() {
        return formMapper;
    }

    @Resource
    protected DynamicFormService dynamicFormService;

    @Override
    @Cacheable(value = CACHE_KEY, key = "#id")
    public Form selectByPk(String id)  {
        return super.selectByPk(id);
    }

    @Override
    public String createNewVersion(String oldVersionId)  {
        Form old = this.selectByPk(oldVersionId);
        assertNotNull(old, "表单不存在!");
        old.setId(RandomUtil.randomChar());
        old.setVersion(old.getVersion() + 1);
        old.setCreateDate(new Date());
        old.setUpdateDate(null);
        old.setRevision(1);
        old.setRelease(0);
        old.setUsing(false);
        getMapper().insert(new InsertParam<>(old));
        return old.getId();
    }

    @Override
    public String insert(Form data)  {
        List<Form> old = this.select(QueryParam.build().where("name", data.getName()));
        Assert.isTrue(old.isEmpty(), "表单 [" + data.getName() + "] 已存在!");
        data.setCreateDate(new Date());
        data.setVersion(1);
        if (StringUtils.isNullOrEmpty(data.getId()))
            data.setId(RandomUtil.randomChar());
        super.insert(data);
        return data.getId();
    }

    @Override
    @Caching(
            evict = {
                    @CacheEvict(value = {CACHE_KEY}, key = "#data.id"),
                    @CacheEvict(value = {CACHE_KEY}, key = "#data.name+':'+#data.version")
            }
    )
    public int update(Form data)  {
        Form old = this.selectByPk(data.getId());
        assertNotNull(old, "表单不存在!");
        data.setUpdateDate(new Date());
        data.setVersion(old.getVersion());
        data.setRevision(old.getRevision() + 1);
        UpdateParam<Form> param = UpdateParam.build(data).excludes("createDate", "release", "version", "using");
        return getMapper().update(param);
    }

    @Override
    @CacheEvict(value = CACHE_KEY, key = "#id")
    public int delete(String id)  {
        Form old = this.selectByPk(id);
        assertNotNull(old, "表单不存在!");
        Assert.isTrue(!old.isUsing(), "表单正在使用，无法删除!");
        return super.delete(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Form> selectLatestList(QueryParam param)  {
        return formMapper.selectLatestList(param);
    }

    @Override
    @Transactional(readOnly = true)
    public int countLatestList(QueryParam param)  {
        return formMapper.countLatestList(param);
    }

    @Override
    @Cacheable(value = CACHE_KEY, key = "#name+':'+#version")
    public Form selectByVersion(String name, int version)  {
        QueryParam param = QueryParam.build()
                .where("name", name).where("version", version);
        List<Form> formList = formMapper.selectLatestList(param);
        return formList.size() > 0 ? formList.get(0) : null;
    }

    @Override
    public Form selectLatest(String name)  {
        QueryParam param = QueryParam.build()
                .where("name", name).orderBy("version").asc();
        List<Form> formList = formMapper.selectLatestList(param);
        return formList.size() > 0 ? formList.get(0) : null;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    @Caching(evict = {
            @CacheEvict(value = {CACHE_KEY + ".deploy"}, key = "'deploy.'+target.selectByPk(#formId).getName()+'.html'"),
            @CacheEvict(value = {CACHE_KEY + ".deploy"}, key = "'deploy.'+target.selectByPk(#formId).getName()"),
            @CacheEvict(value = {CACHE_KEY}, key = "'using.'+target.selectByPk(#formId).getName()")
    })
    public void deploy(String formId) throws Exception {
        Form old = this.selectByPk(formId);
        assertNotNull(old, "表单不存在");
        //先卸载正在使用的表单
        Form using = getMapper().selectUsing(old.getName());
        if (using != null) {
            this.unDeploy(using.getId());
        }
        //开始发布
        old.setUsing(true);
        dynamicFormService.deploy(old);
        old.setRelease(old.getRevision());//发布修订版本
        getMapper().update(UpdateParam.build(old).includes("using", "release").where("id", old.getId()));
        //加入发布历史记录
        History history = History.newInstance("form.deploy." + old.getName());
        history.setPrimaryKeyName("id");
        history.setPrimaryKeyValue(old.getId());
        history.setChangeBefore("{}");
        history.setChangeAfter(JSON.toJSONString(old));
        historyService.insert(history);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    @Caching(evict = {
            @CacheEvict(value = {CACHE_KEY + ".deploy"}, key = "'deploy.'+target.selectByPk(#formId).getName()+'.html'"),
            @CacheEvict(value = {CACHE_KEY + ".deploy"}, key = "'deploy.'+target.selectByPk(#formId).getName()"),
            @CacheEvict(value = {CACHE_KEY}, key = "'using.'+target.selectByPk(#formId).getName()")
    })
    public void unDeploy(String formId)  {
        Form old = this.selectByPk(formId);
        assertNotNull(old, "表单不存在");
        dynamicFormService.unDeploy(old);
        old.setUsing(false);
        UpdateParam param = new UpdateParam<>(old);
        param.includes("using").where("id", old.getId());
        getMapper().update(param);
    }

    @Override
    @Cacheable(value = CACHE_KEY + ".deploy", key = "'deploy.'+#name+'.html'")
    public String createDeployHtml(String name)  {
        History history = historyService.selectLastHistoryByType("form.deploy." + name);
        assertNotNull(history, "表单不存在");
        return formParser.parseHtml(JSON.parseObject(history.getChangeAfter(), Form.class));
    }

    @Override
    @Cacheable(value = CACHE_KEY + ".deploy", key = "'deploy.'+#name")
    public Form selectDeployed(String name)  {
        Form using = selectUsing(name);
        assertNotNull(using, "表单不存在或未部署");
        History history = historyService.selectLastHistoryByType("form.deploy." + name);
        assertNotNull(history, "表单不存在或未部署");
        return JSON.parseObject(history.getChangeAfter(), Form.class);
    }

    @Override
    public String createViewHtml(String formId)  {
        Form form = this.selectByPk(formId);
        assertNotNull(form, "表单不存在");
        return formParser.parseHtml(form);
    }

    @Override
    @Cacheable(value = CACHE_KEY, key = "'using.'+#name")
    public Form selectUsing(String name)  {
        return formMapper.selectUsing(name);
    }
}
