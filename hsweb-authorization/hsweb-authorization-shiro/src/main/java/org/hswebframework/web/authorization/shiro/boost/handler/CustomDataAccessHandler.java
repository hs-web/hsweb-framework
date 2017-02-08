package org.hswebframework.web.authorization.shiro.boost.handler;

import org.hswebframework.web.authorization.access.*;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class CustomDataAccessHandler implements DataAccessHandler {
    @Override
    public boolean isSupport(DataAccess access) {
        return access instanceof CustomDataAccess;
    }

    @Override
    public boolean doAccess(DataAccess access, ParamContext context) {
        CustomDataAccess custom = ((CustomDataAccess) access);
        return custom.getController().doAccess(access, context);
    }
}
