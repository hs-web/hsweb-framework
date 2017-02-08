package org.hswebframework.web.authorization.access;

import java.util.Set;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface FieldAccessController {
    boolean doAccess(String action, Set<FieldAccess> accesses, ParamContext params);
}
