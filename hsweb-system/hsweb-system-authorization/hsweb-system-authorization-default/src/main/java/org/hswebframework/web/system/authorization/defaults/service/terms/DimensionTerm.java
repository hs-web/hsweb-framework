package org.hswebframework.web.system.authorization.defaults.service.terms;

import org.apache.commons.collections4.CollectionUtils;
import org.hswebframework.ezorm.core.Conditional;
import org.hswebframework.ezorm.core.dsl.Query;
import org.hswebframework.ezorm.core.param.QueryParam;
import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.ezorm.rdb.metadata.RDBColumnMetadata;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.BatchSqlFragments;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.EmptySqlFragments;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.PrepareSqlFragments;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.SqlFragments;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.term.AbstractTermFragmentBuilder;
import org.hswebframework.ezorm.rdb.utils.SqlUtils;

import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * 查询和用户维度绑定的数据,如: 查询某个用户的机构
 * where id$dimension$org = userID
 *
 * @author zhouhao
 * @since 4.0.10
 */
public class DimensionTerm extends AbstractTermFragmentBuilder {
    public DimensionTerm() {
        super("dimension", "和维度关联的数据");
    }

    private static final SqlFragments USER_ID_IN = SqlFragments.of("and d.user_id in(");

    public static <T extends Conditional<?>> T inject(T query,
                                                      String column,
                                                      String dimensionType,
                                                      List<String> userId) {
        return inject(query, column, dimensionType, false, false, userId);
    }

    @SuppressWarnings("all")
    public static <T extends Conditional<?>> T inject(T query,
                                                      String column,
                                                      String dimensionType,
                                                      boolean not,
                                                      boolean any,
                                                      List<String> userId) {
        return (T) query.accept(column, createTermType(dimensionType, not, any), userId);
    }

    public static String createTermType(String dimensionType, boolean not, boolean any) {
        StringJoiner joiner = new StringJoiner("$");
        joiner.add("dimension");
        joiner.add(dimensionType);
        if (not) {
            joiner.add("not");
        }
        if (any) {
            joiner.add("any");
        }
        return joiner.toString();
    }

    @Override
    public SqlFragments createFragments(String columnFullName, RDBColumnMetadata column, Term term) {

        List<Object> values = convertList(column, term);
        if (values.isEmpty()) {
            return EmptySqlFragments.INSTANCE;
        }
        List<String> options = term.getOptions();
        if (CollectionUtils.isEmpty(options)) {
            throw new IllegalArgumentException("查询条件错误,正确格式:" + column.getAlias() + "$dimension${type}$[not]");
        }
        BatchSqlFragments fragments = new BatchSqlFragments(6, 2);

        if (options.contains("not")) {
            fragments.add(SqlFragments.NOT);
        }
        fragments
            .addSql("exists(select 1 from",
                    getTableName("s_dimension_user", column),
                    "d where d.dimension_type_id = ? and d.dimension_id =", columnFullName)
            .addParameter(options.get(0));

        if (!options.contains("any")) {
            fragments
                .add(USER_ID_IN)
                .add(SqlUtils.createQuestionMarks(values.size()))
                .add(SqlFragments.RIGHT_BRACKET)
                .addParameter(values);
        }

        fragments.add(SqlFragments.RIGHT_BRACKET);
        return fragments;
    }
}