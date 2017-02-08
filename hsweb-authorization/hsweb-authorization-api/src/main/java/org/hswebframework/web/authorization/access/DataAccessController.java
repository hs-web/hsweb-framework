package org.hswebframework.web.authorization.access;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface DataAccessController {
    boolean doAccess(DataAccess access, ParamContext params);
}
