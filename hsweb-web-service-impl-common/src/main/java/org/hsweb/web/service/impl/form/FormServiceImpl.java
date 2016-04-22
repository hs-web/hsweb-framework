package org.hsweb.web.service.impl.form;

import com.alibaba.fastjson.JSON;
import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.bean.common.UpdateParam;
import org.hsweb.web.bean.po.form.Form;
import org.hsweb.web.bean.po.history.History;
import org.hsweb.web.dao.form.FormMapper;
import org.hsweb.web.service.form.DynamicFormService;
import org.hsweb.web.service.form.FormService;
import org.hsweb.web.service.history.HistoryService;
import org.hsweb.web.service.impl.AbstractServiceImpl;
import org.hsweb.web.utils.RandomUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.webbuilder.utils.common.StringUtils;

import javax.annotation.Resource;
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
    protected FormParser formParser = new CommonFormParser();

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
    public Form selectByPk(String id) throws Exception {
        return super.selectByPk(id);
    }

    @Override
    public String createNewVersion(String oldVersionId) throws Exception {
        Form old = this.selectByPk(oldVersionId);
        Assert.isNull(old, "表单不存在!");
        old.setU_id(RandomUtil.randomChar());
        old.setVersion(old.getVersion() + 1);
        old.setCreate_date(new Date());
        old.setUpdate_date(null);
        return old.getU_id();
    }

    @Override
    public String insert(Form data) throws Exception {
        List<Form> old = this.select(new QueryParam().where("name", data.getName()));
        Assert.isTrue(old.isEmpty(), "表单 [" + data.getName() + "] 已存在!");
        data.setCreate_date(new Date());
        data.setVersion(1);
        if (StringUtils.isNullOrEmpty(data.getU_id()))
            data.setU_id(RandomUtil.randomChar());
        super.insert(data);
        return data.getU_id();
    }

    @Override
    @CacheEvict(value = {CACHE_KEY, CACHE_KEY + "_html_view"}, key = "#data.u_id")
    public int update(Form data) throws Exception {
        Form old = this.selectByPk(data.getU_id());
        Assert.notNull(old, "表单不存在!");
        data.setUpdate_date(new Date());
        return super.update(data);
    }

    @Override
    @CacheEvict(value = CACHE_KEY, key = "#id")
    public int delete(String id) throws Exception {
        Form old = this.selectByPk(id);
        Assert.notNull(old, "表单不存在!");
        Assert.isTrue(!old.isUsing(), "表单正在使用，无法删除!");
        return super.delete(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Form> selectLatestList(QueryParam param) throws Exception {
        return formMapper.selectLatestList(param);
    }

    @Override
    @Transactional(readOnly = true)
    public int countLatestList(QueryParam param) throws Exception {
        return formMapper.countLatestList(param);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    @CacheEvict(value = {CACHE_KEY + "_html"}, allEntries = true)
    public void deploy(String formId) throws Exception {
        Form old = this.selectByPk(formId);
        Assert.notNull(old, "表单不存在");
        //先卸载正在使用的表单
        Form using = getMapper().selectUsing(old.getName());
        if (using != null) {
            this.unDeploy(using.getU_id());
        }
        //开始发布
        old.setUsing(true);
        dynamicFormService.deploy(old);
        getMapper().update(new UpdateParam<>(old).includes("using").where("u_id", old.getU_id()));
        //加入发布历史记录
        History history = History.newInstace("form.deploy." + old.getName());
        history.setPrimary_key_name("u_id");
        history.setPrimary_key_value(old.getU_id());
        history.setChange_before("{}");
        history.setChange_after(JSON.toJSONString(old));
        historyService.insert(history);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void unDeploy(String formId) throws Exception {
        Form old = this.selectByPk(formId);
        Assert.notNull(old, "表单不存在");
        dynamicFormService.unDeploy(old);
        old.setUsing(false);
        UpdateParam param = new UpdateParam<>(old);
        param.includes("using").where("u_id", old.getU_id());
        getMapper().update(param);
    }

    @Override
    @Cacheable(value = CACHE_KEY + "_html", key = "#name")
    public String createDeployHtml(String name) throws Exception {
        History history = historyService.selectLastHistoryByType("form.deploy." + name);
        Assert.notNull(history, "表单不存在");
        return formParser.parseHtml(JSON.parseObject(history.getChange_after(), Form.class));
    }

    @Override
    @Cacheable(value = CACHE_KEY + "_html_view", key = "#name")
    public String createViewHtml(String formId) throws Exception {
        Form form = this.selectByPk(formId);
        Assert.notNull(form, "表单不存在");
        return formParser.parseHtml(form);
    }
}
