package org.hsweb.web.service.impl.form;

import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.bean.common.UpdateParam;
import org.hsweb.web.bean.po.form.Form;
import org.hsweb.web.dao.GenericMapper;
import org.hsweb.web.dao.form.FormMapper;
import org.hsweb.web.exception.BusinessException;
import org.hsweb.web.service.form.DynamicFormService;
import org.hsweb.web.service.form.FormService;
import org.hsweb.web.service.impl.AbstractServiceImpl;
import org.hsweb.web.utils.RandomUtil;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

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

    @Resource
    private FormMapper formMapper;

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
        Assert.notNull(old, "表单 [" + data.getName() + "] 已存在!");
        data.setCreate_date(new Date());
        data.setVersion(1);
        super.insert(data);
        return data.getU_id();
    }

    @Override
    @CacheEvict(value = CACHE_KEY, key = "#data.u_id")
    public int update(Form data) throws Exception {
        Form old = this.selectByPk(data.getU_id());
        Assert.isNull(old, "表单不存在!");
        data.setUpdate_date(new Date());
        return super.update(data);
    }

    @Override
    @CacheEvict(value = CACHE_KEY, key = "#id")
    public int delete(String id) throws Exception {
        Form old = this.selectByPk(id);
        Assert.isNull(old, "表单不存在!");
        Assert.isTrue(old.isUsing(), "表单正在使用，无法删除!");
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
    public String createHtml(String formId) throws Exception {
        return null;
    }
}
