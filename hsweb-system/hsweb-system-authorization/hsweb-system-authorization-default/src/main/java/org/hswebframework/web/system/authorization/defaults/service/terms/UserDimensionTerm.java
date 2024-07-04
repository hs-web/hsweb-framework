package org.hswebframework.web.system.authorization.defaults.service.terms;

import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.ezorm.rdb.metadata.RDBColumnMetadata;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.BatchSqlFragments;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.EmptySqlFragments;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.SqlFragments;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.term.AbstractTermFragmentBuilder;
import org.hswebframework.ezorm.rdb.utils.SqlUtils;

import java.util.List;

/**
 * 查询和用户维度绑定的数据,如: 查询机构下的用户
 * where id$in-dimension$org = orgId
 *
 * @author zhouhao
 * @since 4.0.10
 */
public class UserDimensionTerm extends AbstractTermFragmentBuilder {
    public UserDimensionTerm() {
        super("in-dimension", "在维度中的用户数据");
    }

    static SqlFragments DIMENSION_ID_IN = SqlFragments.of("and d.dimension_id in(");
    static SqlFragments DIMENSION_TYPE_ID = SqlFragments.of("and d.dimension_type_id = ?");

    @Override
    public SqlFragments createFragments(String columnFullName, RDBColumnMetadata column, Term term) {

        List<Object> values = convertList(column, term);
        if (values.isEmpty()) {
            return EmptySqlFragments.INSTANCE;
        }

        BatchSqlFragments fragments = new BatchSqlFragments(7,2);
        List<String> options = term.getOptions();

        if (options.contains("not")) {
            fragments.add(SqlFragments.NOT);
        }

        fragments.addSql("exists(select 1 from",
                         getTableName("s_dimension_user", column),
                         "d where d.user_id =", columnFullName);

        if (!options.isEmpty()) {
            String typeId = options.get(0);
            if (!"not".equals(typeId) && !"any".equals(typeId)) {
                fragments.add(DIMENSION_TYPE_ID).addParameter(typeId);
            }
        }

        if (!options.contains("any")) {
            fragments
                .add(DIMENSION_ID_IN)
                .add(SqlUtils.createQuestionMarks(values.size()))
                .add(SqlFragments.RIGHT_BRACKET)
                .addParameter(values);
        }
        fragments.add(SqlFragments.RIGHT_BRACKET);

        return fragments;
    }
}
