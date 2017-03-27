package org.hswebframework.web.authorization.shiro.boost;

import org.hswebframework.web.authorization.access.DataAccessConfig;
import org.hswebframework.web.authorization.access.DataAccessController;
import org.hswebframework.web.authorization.access.DataAccessHandler;
import org.hswebframework.web.authorization.shiro.boost.handler.CustomDataAccessHandler;
import org.hswebframework.web.authorization.shiro.boost.handler.OwnCreatedDataAccessHandler;
import org.hswebframework.web.authorization.shiro.boost.handler.ScriptDataAccessHandler;
import org.hswebframework.web.boost.aop.context.MethodInterceptorParamContext;

import java.util.LinkedList;
import java.util.List;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public final class DefaultDataAccessController implements DataAccessController {

    private DataAccessController parent;

    private List<DataAccessHandler> handlers = new LinkedList<>();

    public DefaultDataAccessController() {
        this(null);
    }

    public DefaultDataAccessController(DataAccessController parent) {
        if (parent == this) throw new UnsupportedOperationException();
        this.parent = parent;
        addHandler(new CustomDataAccessHandler());
        addHandler(new OwnCreatedDataAccessHandler());
        addHandler(new ScriptDataAccessHandler());
    }

    @Override
    public boolean doAccess(DataAccessConfig access, MethodInterceptorParamContext params) {
        if (parent != null) parent.doAccess(access, params);
        return handlers.parallelStream()
                .filter(handler -> handler.isSupport(access))
                .anyMatch(handler -> handler.handle(access, params));
    }

    public DefaultDataAccessController addHandler(DataAccessHandler handler) {
        handlers.add(handler);
        return this;
    }

    public void setHandlers(List<DataAccessHandler> handlers) {
        this.handlers = handlers;
    }

    public List<DataAccessHandler> getHandlers() {
        return handlers;
    }
}
