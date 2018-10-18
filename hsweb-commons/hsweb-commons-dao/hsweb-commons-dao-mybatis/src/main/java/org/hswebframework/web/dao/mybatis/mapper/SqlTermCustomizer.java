package org.hswebframework.web.dao.mybatis.mapper;

import org.hswebframework.ezorm.rdb.render.dialect.Dialect;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
public interface SqlTermCustomizer extends Dialect.TermTypeMapper {
    String getTermType();

    Dialect[] forDialect();
}
