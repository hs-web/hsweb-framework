package org.hswebframework.web.service.authorization.simple.access;

import com.alibaba.fastjson.JSON;
import org.hswebframework.web.authorization.access.DataAccessConfig;
import org.hswebframework.web.entity.authorization.DataAccessEntity;
import org.hswebframework.web.service.authorization.DataAccessFactory;
import org.springframework.stereotype.Component;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@Component("simpleDataAccessFactory")
public class SimpleDataAccessFactory implements DataAccessFactory {

    @Override
    public DataAccessConfig create(DataAccessEntity entity) {
        AbstractDataAccess dataAccess = null;
        try {
            switch (entity.getType().toUpperCase()) {
                case "CUSTOM":
                    return dataAccess = new SimpleCustomDataAccess(entity.getConfig());
                case "SCRIPT":
                    return dataAccess = JSON.parseObject(entity.getConfig(), SimpleScriptDataAccess.class);
                case "OWN_CREATED":
                    return dataAccess = new SimpleOwnCreatedDataAccess();
            }
        } finally {
            if (null != dataAccess) dataAccess.setAction(entity.getAction());
        }
        return createOtherType(entity);
    }

    protected DataAccessConfig createOtherType(DataAccessEntity entity) {
        return null;
    }
}
