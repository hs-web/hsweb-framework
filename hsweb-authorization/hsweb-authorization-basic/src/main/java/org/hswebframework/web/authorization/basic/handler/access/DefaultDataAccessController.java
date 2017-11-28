package org.hswebframework.web.authorization.basic.handler.access;

import org.hswebframework.web.authorization.access.DataAccessConfig;
import org.hswebframework.web.authorization.access.DataAccessController;
import org.hswebframework.web.authorization.access.DataAccessHandler;
import org.hswebframework.web.authorization.define.AuthorizingContext;

import java.util.LinkedList;
import java.util.List;

/**
 * 默认的行级权限控制.通过获取DataAccessHandler进行实际处理
 *
 * @author zhouhao
 * @see DataAccessHandler
 * @since 3.0
 */
public final class DefaultDataAccessController implements DataAccessController {

    private DataAccessController parent;

    private List<DataAccessHandler> handlers = new LinkedList<>();

    public DefaultDataAccessController() {
        this(null);
    }

    public DefaultDataAccessController(DataAccessController parent) {
        if (parent == this) {
            throw new UnsupportedOperationException();
        }
        this.parent = parent;
        addHandler(new CustomDataAccessHandler()).
                addHandler(new OwnCreatedDataAccessHandler()).
                addHandler(new ScriptDataAccessHandler()).
                addHandler(new FieldFilterDataAccessHandler()).
                addHandler(new FieldScopeDataAccessHandler());
    }

    @Override
    public boolean doAccess(DataAccessConfig access, AuthorizingContext context) {
        if (parent != null) {
            parent.doAccess(access, context);
        }
        return handlers.stream()
                .filter(handler -> handler.isSupport(access))
                .allMatch(handler -> handler.handle(access, context));
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
