package org.hswebframework.web.system.authorization.defaults.service.terms;

import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.ezorm.rdb.metadata.RDBColumnMetadata;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.EmptySqlFragments;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.PrepareSqlFragments;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.SqlFragments;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.term.AbstractTermFragmentBuilder;

import java.util.List;
import java.util.stream.Collectors;

public class UserDimensionTerm extends AbstractTermFragmentBuilder {
    public UserDimensionTerm() {
        super("in-dimension", "在维度中的用户");
    }

    @Override
    public SqlFragments createFragments(String columnFullName, RDBColumnMetadata column, Term term) {

        List<Object> values = convertList(column, term);
        if (values.isEmpty()) {
            return EmptySqlFragments.INSTANCE;
        }

        PrepareSqlFragments fragments = PrepareSqlFragments.of();

        fragments.addSql("exists(select 1 from s_dimension_user d where d.user_id =", columnFullName, "and d.dimension_id in(",
                values.stream().map(r -> "?").collect(Collectors.joining(",")), "))")
                .addParameter(values);


        return fragments;
    }
}
