package org.hswebframework.web.service.authorization.simple;

import com.alibaba.fastjson.JSON;
import org.hswebframework.web.authorization.access.DataAccessConfig;
import org.hswebframework.web.authorization.builder.DataAccessConfigBuilderFactory;
import org.hswebframework.web.entity.authorization.DataAccessEntity;
import org.hswebframework.web.service.authorization.DataAccessFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@Component
public class DefaultDataAccessFactory implements DataAccessFactory {
    private DataAccessConfigBuilderFactory dataAccessConfigBuilderFactory;

    @Autowired
    public void setDataAccessConfigBuilderFactory(DataAccessConfigBuilderFactory dataAccessConfigBuilderFactory) {
        this.dataAccessConfigBuilderFactory = dataAccessConfigBuilderFactory;
    }

    @Override
    public DataAccessConfig create(DataAccessEntity entity) {
        return dataAccessConfigBuilderFactory.create().fromJson(JSON.toJSONString(entity)).build();
    }
}
