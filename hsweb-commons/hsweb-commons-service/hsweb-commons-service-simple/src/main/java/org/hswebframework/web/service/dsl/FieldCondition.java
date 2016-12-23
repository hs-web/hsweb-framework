package org.hswebframework.web.service.dsl;


import java.util.Collection;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface FieldCondition<PO, R extends DSLQuery<PO>> {

    R accept(String termType, Object value);

    R is(Object value);

    R like(Object value);

    R like$(Object value);

    R $like(Object value);

    R $like$(Object value);

    R notLike(Object value);

    R gt(Object value);

    R lt(Object value);

    R gte(Object value);

    R lte(Object value);

    R in(Object value);

    R in(Object... values);

    R in(Collection values);

    R notIn(Object value);

    R isEmpty();

    R notEmpty();

    R isNull();

    R notNull();

    R not(Object value);

    R between(Object between, Object and);

    R notBetween(Object between, Object and);

}
