package org.hswebframework.web.service.terms;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.ezorm.rdb.metadata.RDBColumnMetadata;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.PrepareSqlFragments;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.SqlFragments;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.function.FunctionFragmentBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@Slf4j
public abstract class TreeStructureSqlTermCustomizer extends AbstractSqlTermCustomizer {
    public TreeStructureSqlTermCustomizer(String termType) {
        super(termType);
    }

    protected abstract String getTableName();

    protected abstract List<String> getTreePathByTerm(List<Object> termValue);

    @Override
    public SqlFragments createFragments(String columnFullName, RDBColumnMetadata column, Term term) {

        List<Object> listValue = convertList(term.getValue());
        List<String> paths = getTreePathByTerm(listValue)
                .stream()
                .map(path -> path.concat("%"))
                .collect(Collectors.toList());
        List<String> options = term.getOptions();

        boolean parent = options.contains("parent");
        boolean not = options.contains("not");

        PrepareSqlFragments fragments = PrepareSqlFragments.of()
                .addSql(not ? "not " : "", "exists(select 1 from ", getTableName(), " tmp where tmp.u_id = ", columnFullName);

        int len = paths.size();

        if (len > 0) {
            fragments.addSql(" and (");
        }
        for (int i = 0; i < len; i++) {
            if (i > 0) {
                fragments.addSql(" or");
            }
            if (parent) {
                FunctionFragmentBuilder function = column.findFeature(FunctionFragmentBuilder.createFeatureId("concat")).orElse(null);

                String concat;
                if (function == null) {
                    concat = getTableName() + ".path";
                    log.warn("数据库不支持concat函数(FunctionFragmentBuilder)!");
                    fragments.addSql("? like ", paths.get(i)).addSql("like", concat);
                } else {
                    Map<String, Object> param = new HashMap<>();
                    param.put("0", "'tmp.path'");
                    param.put("1", "'%'");

                    //? like concat(tmp.path,'%')
                    fragments.addSql("? like ", paths.get(i)).addSql("like").addFragments(function.create("tmp.path", column, param));
                }

            } else {
                fragments.addSql("tmp.path like ?", paths.get(i));
            }
        }
        if (len > 0) {
            fragments.addSql(")");
        }
        fragments.addSql(")");

        return fragments;
    }

}