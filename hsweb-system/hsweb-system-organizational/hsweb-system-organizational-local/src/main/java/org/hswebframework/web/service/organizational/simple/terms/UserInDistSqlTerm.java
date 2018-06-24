package org.hswebframework.web.service.organizational.simple.terms;

import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.ezorm.rdb.meta.RDBColumnMetaData;
import org.hswebframework.ezorm.rdb.render.SqlAppender;
import org.hswebframework.ezorm.rdb.render.dialect.Dialect;
import org.hswebframework.ezorm.rdb.render.dialect.term.BoostTermTypeMapper;
import org.hswebframework.web.dao.mybatis.mapper.ChangedTermValue;
import org.hswebframework.web.service.organizational.DistrictService;

import java.util.List;


/**
 * 查询岗位中的用户
 *
 * @author zhouhao
 * @since 3.0.0-RC
 */
public class UserInDistSqlTerm extends UserInSqlTerm {

    private boolean not;

    public UserInDistSqlTerm(boolean not, boolean child, String term, DistrictService service) {
        super(term, service);
        setChild(child);
        this.not = not;
    }

    @Override
    public String getTableName() {
        return "_dist";
    }

    @Override
    public SqlAppender accept(String wherePrefix, Term term, RDBColumnMetaData column, String tableAlias) {
        ChangedTermValue termValue = createChangedTermValue(term);
        Dialect dialect=column.getTableMetaData().getDatabaseMetaData().getDialect();

        SqlAppender appender = new SqlAppender();
        appender.addSpc(not ? "not" : "", "exists(select 1 from s_person_position _tmp,s_position _pos,s_person _person,s_department _dept,s_organization _org");
        if (isChild()||isParent()) {
            appender.addSpc(",s_district _dist");
        }
        appender.addSpc("where _person.u_id=_tmp.person_id and _tmp.position_id = _pos.u_id and _person.u_id=_tmp.person_id and _dept.u_id=_pos.department_id and _org.u_id=_dept.org_id"
                , "and", createColumnName(column, tableAlias), "=", isForPerson() ? "_tmp.person_id" : "_person.user_id");
        if (isChild()||isParent()) {
            appender.addSpc("and _org.district_id=_dist.u_id");
        }
        List<Object> positionIdList = BoostTermTypeMapper.convertList(column, termValue.getOld());
        if (!positionIdList.isEmpty()) {
            appender.addSpc("and");
            termValue.setValue(appendCondition(positionIdList, wherePrefix, appender, "_org.district_id",dialect));
        }

        appender.add(")");
        return appender;
    }
}
