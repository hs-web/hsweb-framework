package org.hswebframework.web.system.authorization.defaults.service.terms;

import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.ezorm.rdb.metadata.RDBColumnMetadata;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.EmptySqlFragments;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.PrepareSqlFragments;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.SqlFragments;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.term.AbstractTermFragmentBuilder;

import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public SqlFragments createFragments(String columnFullName, RDBColumnMetadata column, Term term) {

        List<Object> values = convertList(column, term);
        if (values.isEmpty()) {
            return EmptySqlFragments.INSTANCE;
        }

        PrepareSqlFragments fragments = PrepareSqlFragments.of();
        List<String> options = term.getOptions();

        if (options.contains("not")) {
            fragments.addSql("not");
        }

        fragments.addSql("exists(select 1 from s_dimension_user d where d.user_id =", columnFullName);

        if (options.size() > 0) {
            String typeId = options.get(0);
            if (!"not".equals(typeId) && !"any".equals(typeId)) {
                fragments.addSql("and d.dimension_type_id = ?").addParameter(typeId);
            }
        }

        if (!options.contains("any")) {
            fragments.addSql("and d.dimension_id in(",
                             values.stream().map(r -> "?").collect(Collectors.joining(",")), ")")
                     .addParameter(values);
        }
        fragments.addSql(")");

        return fragments;
    }
}
