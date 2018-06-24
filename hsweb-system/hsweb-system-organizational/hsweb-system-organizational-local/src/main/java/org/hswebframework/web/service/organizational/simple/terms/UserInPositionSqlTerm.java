package org.hswebframework.web.service.organizational.simple.terms;

import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.ezorm.rdb.meta.RDBColumnMetaData;
import org.hswebframework.ezorm.rdb.render.SqlAppender;
import org.hswebframework.ezorm.rdb.render.dialect.Dialect;
import org.hswebframework.ezorm.rdb.render.dialect.term.BoostTermTypeMapper;
import org.hswebframework.web.dao.mybatis.mapper.ChangedTermValue;
import org.hswebframework.web.service.organizational.PositionService;

import java.util.List;


/**
 * 查询岗位中的用户
 *
 * @author zhouhao
 * @since 3.0.0-RC
 */
public class UserInPositionSqlTerm extends UserInSqlTerm {

    private boolean not;

    public UserInPositionSqlTerm(boolean not, boolean child, String term, PositionService positionService) {
        super(term, positionService);
        setChild(child);
        this.not = not;
    }

    @Override
    public String getTableName() {
        return "_pos";
    }

    @Override
    public SqlAppender accept(String wherePrefix, Term term, RDBColumnMetaData column, String tableAlias) {
        ChangedTermValue termValue = createChangedTermValue(term);
        Dialect dialect=column.getTableMetaData().getDatabaseMetaData().getDialect();

        SqlAppender appender = new SqlAppender();
        appender.addSpc(not ? "not" : "", "exists(select 1 from s_person_position _tmp");
        if (isChild()||isParent()) {
            appender.addSpc(",s_position _pos");
        }
        if (!isForPerson()) {
            appender.addSpc(",s_person _person");
        }

        appender.addSpc("where ",
                createColumnName(column, tableAlias), "=",
                isForPerson() ? " _tmp.person_id" : "_person.user_id and _person.u_id=_tmp.person_id");

        if (isChild()||isParent()) {
            appender.addSpc("and _pos.u_id=_tmp.position_id");
        }

        List<Object> positionIdList = BoostTermTypeMapper.convertList(column, termValue.getOld());
        if (!positionIdList.isEmpty()) {
            appender.addSpc("and");
            termValue.setValue(appendCondition(positionIdList, wherePrefix, appender, "_tmp.position_id",dialect));
        }

        appender.add(")");

        return appender;
    }
}
