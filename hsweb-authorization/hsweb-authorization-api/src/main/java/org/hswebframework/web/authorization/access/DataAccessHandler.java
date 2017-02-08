package org.hswebframework.web.authorization.access;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface DataAccessHandler {

    boolean isSupport(DataAccess access);

    boolean doAccess(DataAccess access, ParamContext context);
}
