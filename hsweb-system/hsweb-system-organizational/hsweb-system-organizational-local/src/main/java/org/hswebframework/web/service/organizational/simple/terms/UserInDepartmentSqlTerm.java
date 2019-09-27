package org.hswebframework.web.service.organizational.simple.terms;

import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.ezorm.rdb.metadata.RDBColumnMetadata;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.PrepareSqlFragments;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.SqlFragments;
import org.hswebframework.web.service.organizational.DepartmentService;

import java.util.List;


/**
 * @author zhouhao
 * @since 3.1
 */
public class UserInDepartmentSqlTerm extends UserInSqlTerm {

    public UserInDepartmentSqlTerm(String term, DepartmentService departmentService) {
        super(term, departmentService);
    }

    @Override
    public String getTableName() {
        return "_dept";
    }

    @Override
    public SqlFragments createFragments(String columnFullName, RDBColumnMetadata column, Term term) {
        PrepareSqlFragments fragments = PrepareSqlFragments.of();
        boolean not = term.getOptions().contains("not");
        boolean child = term.getOptions().contains("child");
        boolean parent = term.getOptions().contains("parent");

        fragments.addSql(not ? "not" : "", "exists(select 1 from ",
                getTableFullName("s_person_position"), " _tmp,",
                getTableFullName("s_position"), " _pos,",
                getTableFullName("s_person"), " _person");
        if (child || parent) {
            fragments.addSql(",", getTableFullName("s_department"), " _dept");
        }
        fragments.addSql("where _person.u_id=_tmp.person_id and _tmp.position_id = _pos.u_id and _person.u_id=_tmp.person_id"
                , "and", columnFullName, "=", isForPerson() ? "_tmp.person_id" : "_person.user_id");
        if (child || parent) {
            fragments.addSql("and _dept.u_id=_pos.department_id");
        }
        List<Object> positionIdList = convertList(term.getValue());
        if (!positionIdList.isEmpty()) {
            fragments.addSql("and");
            appendCondition("_pos.department_id", fragments, column, term, positionIdList);
        }
        fragments.addSql(")");
        return fragments;
    }


    @Override
    public String getName() {
        return "根据" + (isForPerson() ? "人员" : "用户") + "按部门查询";
    }
}
