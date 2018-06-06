package org.hswebframework.web.service.organizational.simple.terms;

import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.ezorm.rdb.meta.RDBColumnMetaData;
import org.hswebframework.ezorm.rdb.render.SqlAppender;
import org.hswebframework.ezorm.rdb.render.dialect.term.BoostTermTypeMapper;
import org.hswebframework.web.dao.mybatis.mapper.AbstractSqlTermCustomer;

import java.util.List;


/**
 * 查询岗位中的用户
 *
 * @author zhouhao
 * @since 3.0.0-RC
 */
public class UserInPositionSqlTerm extends AbstractSqlTermCustomer {

    private boolean not;

    public UserInPositionSqlTerm(boolean not) {
        super("user-" + (not ? "not-" : "") + "in-position");
        this.not = not;
    }

    @Override
    public SqlAppender accept(String wherePrefix, Term term, RDBColumnMetaData column, String tableAlias) {
        SqlAppender appender = new SqlAppender();
        //exists(
        // select 1 from s_person_position tmp,s_person u
        // where
        // u.u_id=tmp.person_id
        // and {column}=u.user_id
        // and position_id =?
        // )
        appender.addSpc(not ? "not" : "", "exists(select 1 from s_person_position tmp,s_person u"
                , "where u.u_id=tmp.person_id"
                , "and", createColumnName(column, tableAlias), "=u.user_id");

        List<Object> positionIdList = BoostTermTypeMapper.convertList(column, term.getValue());
        if (!positionIdList.isEmpty()) {
            appender.addSpc("and tmp.position_id");
            appendCondition(positionIdList, wherePrefix, appender);
            term.setValue(positionIdList);
        }

        appender.add(")");

        return appender;
    }
}
