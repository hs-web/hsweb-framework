package org.hswebframework.web.service.dsl;

import org.hsweb.ezorm.core.dsl.Query;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.service.DefaultQueryByEntityService;

import java.util.List;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimpleDSLQuery<PO> implements DSLQuery<PO> {
    protected Query<PO, QueryParamEntity> query = Query.empty(new QueryParamEntity());

    public SimpleDSLQuery(DefaultQueryByEntityService<PO> service) {
        query.setListExecutor(service::select);
        query.setTotalExecutor(service::count);
        query.setSingleExecutor(service::selectSingle);
    }

    public Query<PO, QueryParamEntity> dynamic() {
        return query;
    }

    @Override
    public List<PO> list() {
        return query.list();
    }

    @Override
    public List<PO> list(int pageIndex, int pageSize) {
        return query.list(pageIndex, pageSize);
    }

    @Override
    public PO single() {
        return query.single();
    }

    @Override
    public int count() {
        return query.total();
    }
}
