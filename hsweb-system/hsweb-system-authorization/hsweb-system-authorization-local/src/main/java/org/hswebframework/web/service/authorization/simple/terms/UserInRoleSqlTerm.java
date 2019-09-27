package org.hswebframework.web.service.authorization.simple.terms;

import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.ezorm.rdb.metadata.RDBColumnMetadata;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.PrepareSqlFragments;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.SqlFragments;
import org.hswebframework.web.service.terms.AbstractSqlTermCustomizer;

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
    public SqlFragments createFragments(String columnFullName, RDBColumnMetadata column, Term term) {
        PrepareSqlFragments fragments = PrepareSqlFragments.of();

        fragments.addSql(not ? "not " : "", "exists(select 1 from s_user_role tmp where tmp.user_id =", columnFullName);

        List<Object> positionIdList = convertList(term.getValue());
        if (!positionIdList.isEmpty()) {
            fragments.addSql(" and tmp.role_id");
            appendCondition(fragments, column, positionIdList);
        }
        fragments.addSql(")");

        return fragments;
    }

    @Override
    public String getName() {
        return "按角色查询用户";
    }
}
