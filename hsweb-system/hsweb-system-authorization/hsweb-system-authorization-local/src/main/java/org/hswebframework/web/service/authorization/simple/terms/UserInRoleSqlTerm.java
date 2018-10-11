package org.hswebframework.web.service.authorization.simple.terms;

import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.ezorm.rdb.meta.RDBColumnMetaData;
import org.hswebframework.ezorm.rdb.render.SqlAppender;
import org.hswebframework.ezorm.rdb.render.dialect.term.BoostTermTypeMapper;
import org.hswebframework.web.dao.mybatis.mapper.AbstractSqlTermCustomizer;
import org.hswebframework.web.dao.mybatis.mapper.ChangedTermValue;

import java.util.List;

/**
 * @author zhouhao
 * @since 3.0
 */
public class UserInRoleSqlTerm extends AbstractSqlTermCustomizer {

    private boolean not;

    public UserInRoleSqlTerm(boolean not) {
        super("user" + (not ? "-not-in" : "-in") + "-role");
        this.not = not;
    }

    @Override
    public SqlAppender accept(String wherePrefix, Term term, RDBColumnMetaData column, String tableAlias) {
        ChangedTermValue termValue = createChangedTermValue(term);
        SqlAppender appender = new SqlAppender();
        appender.add(not ? "not " : "", "exists(select 1 from s_user_role tmp where tmp.user_id =",
                createColumnName(column, tableAlias));

        List<Object> positionIdList = BoostTermTypeMapper.convertList(column, termValue.getOld());
        if (!positionIdList.isEmpty()) {
            appender.add(" and tmp.role_id");
            termValue.setValue(appendCondition(positionIdList, wherePrefix, appender));
        }
        appender.add(")");

        return appender;
    }
}
