package org.hsweb.web.service.impl.form;

import org.hsweb.web.bean.po.form.Form;
import org.hsweb.web.dao.GenericMapper;
import org.hsweb.web.dao.form.FormMapper;
import org.hsweb.web.exception.BusinessException;
import org.hsweb.web.service.form.FormService;
import org.hsweb.web.service.impl.AbstractServiceImpl;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

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

    @Override
    @Cacheable(value = CACHE_KEY, key = "#id")
    public Form selectByPk(String id) throws Exception {
        return super.selectByPk(id);
    }

    @Override
    public String insert(Form data) throws Exception {
        Form old = this.selectByPk(data.getU_id());
        if (old != null)
            throw new BusinessException("该表单已存在!");
        data.setCreate_date(new Date());
        super.insert(data);
        return data.getU_id();
    }

    @Override
    @CacheEvict(value = CACHE_KEY, key = "#data.u_id")
    public int update(Form data) throws Exception {
        Form old = this.selectByPk(data.getU_id());
        if (old == null)
            throw new BusinessException("该表单不存在!");
        data.setUpdate_date(new Date());
        return super.update(data);
    }

    @Override
    public int delete(String s) throws Exception {
        throw new BusinessException("此服务已关闭!");
    }


}
