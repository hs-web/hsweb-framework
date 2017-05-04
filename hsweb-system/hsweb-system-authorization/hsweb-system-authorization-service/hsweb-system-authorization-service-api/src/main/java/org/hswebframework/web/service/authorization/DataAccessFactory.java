package org.hswebframework.web.service.authorization;

import org.hswebframework.web.authorization.access.DataAccessConfig;
import org.hswebframework.web.entity.authorization.DataAccessEntity;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface DataAccessFactory {
    DataAccessConfig create(DataAccessEntity entity);
}
