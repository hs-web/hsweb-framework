package org.hswebframework.web.service.dsl;

import org.hsweb.ezorm.core.dsl.Query;
import org.hswebframework.web.commons.beans.param.QueryParamBean;

import java.util.Collection;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimpleFieldCondition<PO, R extends DSLQuery<PO>> implements FieldCondition<PO, R> {
    Query<PO, QueryParamBean> query;
    R                         proxy;
    String                    filed;

    public SimpleFieldCondition(Query<PO, QueryParamBean> query, R proxy, String filed) {
        this.query = query;
        this.proxy = proxy;
        this.filed = filed;
    }

    @Override
    public R accept(String termType, Object value) {
        query.accept(filed, termType, value);
        return proxy;
    }

    @Override
    public R is(Object value) {
        query.is(filed, value);
        return proxy;
    }

    @Override
    public R like(Object value) {
        query.like(filed, value);
        return proxy;
    }

    @Override
    public R like$(Object value) {
        query.like$(filed, value);
        return proxy;
    }

    @Override
    public R $like(Object value) {
        query.$like(filed, value);
        return proxy;
    }

    @Override
    public R $like$(Object value) {
        query.$like$(filed, value);
        return proxy;
    }

    @Override
    public R notLike(Object value) {
        query.notLike(filed, value);
        return proxy;
    }

    @Override
    public R gt(Object value) {
        query.gt(filed, value);
        return proxy;
    }

    @Override
    public R lt(Object value) {
        query.lt(filed, value);
        return proxy;
    }

    @Override
    public R gte(Object value) {
        query.gte(filed, value);
        return proxy;
    }

    @Override
    public R lte(Object value) {
        query.lte(filed, value);
        return proxy;
    }

    @Override
    public R in(Object value) {
        query.in(filed, value);
        return proxy;
    }

    @Override
    public R in(Object... values) {
        query.in(filed, values);
        return proxy;
    }

    @Override
    public R in(Collection values) {
        query.in(filed, values);
        return proxy;
    }

    @Override
    public R notIn(Object value) {
        query.is(filed, value);
        return proxy;
    }

    @Override
    public R isEmpty() {
        query.isEmpty(filed);
        return proxy;
    }

    @Override
    public R notEmpty() {
        query.notEmpty(filed);
        return proxy;
    }

    @Override
    public R isNull() {
        query.isNull(filed);
        return proxy;
    }

    @Override
    public R notNull() {
        query.notNull(filed);
        return proxy;
    }

    @Override
    public R not(Object value) {
        query.not(filed, value);
        return proxy;
    }

    @Override
    public R between(Object between, Object and) {
        query.between(filed, between, and);
        return proxy;
    }

    @Override
    public R notBetween(Object between, Object and) {
        query.notBetween(filed, between, and);
        return proxy;
    }
}
