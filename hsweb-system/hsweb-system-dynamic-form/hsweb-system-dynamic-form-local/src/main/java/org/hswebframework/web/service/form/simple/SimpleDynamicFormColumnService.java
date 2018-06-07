package org.hswebframework.web.service.form.simple;

import org.hswebframework.web.dao.form.DynamicFormColumnDao;
import org.hswebframework.web.entity.form.DynamicFormColumnEntity;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.form.DynamicFormColumnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("dynamicFormColumnService")
public class SimpleDynamicFormColumnService extends GenericEntityService<DynamicFormColumnEntity, String>
        implements DynamicFormColumnService {
    @Autowired
    private DynamicFormColumnDao dynamicFormColumnDao;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public DynamicFormColumnDao getDao() {
        return dynamicFormColumnDao;
    }

}
