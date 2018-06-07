package org.hswebframework.web.service.organizational.simple.terms;

import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.ezorm.rdb.meta.RDBColumnMetaData;
import org.hswebframework.ezorm.rdb.render.SqlAppender;
import org.hswebframework.ezorm.rdb.render.dialect.term.BoostTermTypeMapper;
import org.hswebframework.web.dao.mybatis.mapper.AbstractSqlTermCustomer;
import org.hswebframework.web.dao.mybatis.mapper.ChangedTermValue;

import java.util.List;


/**
 * 查询岗位中的人员
 *
 * @author zhouhao
 * @since 3.0.0-RC
 */
public class PersonInPositionSqlTerm extends AbstractSqlTermCustomer {

    private boolean not;

    public PersonInPositionSqlTerm(boolean not) {
        super("person-" + (not ? "not-" : "") + "in-position");
        this.not = not;
    }

    @Override
    public SqlAppender accept(String wherePrefix, Term term, RDBColumnMetaData column, String tableAlias) {
        ChangedTermValue termValue = createChangedTermValue(term);

        SqlAppender appender = new SqlAppender();
        //exists(select 1 from s_person_position tmp where tmp.person_id = t.owner_id and position_id =?)
        appender.add(not ? "not " : "", "exists(select 1 from s_person_position tmp where tmp.person_id =", createColumnName(column, tableAlias));

        List<Object> positionIdList = BoostTermTypeMapper.convertList(column, termValue.getOld());
        if (!positionIdList.isEmpty()) {
            appender.add(" and tmp.position_id");
            termValue.setValue(appendCondition(positionIdList, wherePrefix, appender));
        }

        appender.add(")");

        return appender;
    }
}
