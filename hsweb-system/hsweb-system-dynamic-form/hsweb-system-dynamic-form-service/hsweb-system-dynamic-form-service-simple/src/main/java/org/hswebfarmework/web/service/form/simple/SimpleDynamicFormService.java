package org.hswebfarmework.web.service.form.simple;

import org.hswebfarmework.web.dao.form.DynamicFormDao;
import org.hswebfarmework.web.entity.form.DynamicFormEntity;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.id.IDGenerator;
import org.hswebfarmework.web.service.form.DynamicFormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("dynamicFormService")
public class SimpleDynamicFormService extends GenericEntityService<DynamicFormEntity, String>
        implements DynamicFormService {
    @Autowired
    private DynamicFormDao dynamicFormDao;
   @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }
    @Override
    public DynamicFormDao getDao() {
        return dynamicFormDao;
    }

}
