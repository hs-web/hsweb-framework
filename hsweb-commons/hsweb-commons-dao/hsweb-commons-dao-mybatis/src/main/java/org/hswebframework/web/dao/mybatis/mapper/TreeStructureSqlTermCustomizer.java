package org.hswebframework.web.dao.mybatis.mapper;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.ezorm.rdb.meta.RDBColumnMetaData;
import org.hswebframework.ezorm.rdb.render.SqlAppender;
import org.hswebframework.ezorm.rdb.render.dialect.Dialect;
import org.hswebframework.ezorm.rdb.render.dialect.RenderPhase;
import org.hswebframework.ezorm.rdb.render.dialect.function.SqlFunction;
import org.hswebframework.ezorm.rdb.render.dialect.term.BoostTermTypeMapper;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@Slf4j
public abstract class TreeStructureSqlTermCustomizer extends AbstractSqlTermCustomizer {
    boolean not = false;

    boolean parent = false;

    public TreeStructureSqlTermCustomizer(String termType, boolean not, boolean parent) {
        super(termType);
        this.not = not;
    }

    protected abstract String getTableName();

    protected abstract List<String> getTreePathByTerm(List<Object> termValue);

    @Override
    public SqlAppender accept(String wherePrefix, Term term, RDBColumnMetaData column, String tableAlias) {
        ChangedTermValue termValue = createChangedTermValue(term);
        Dialect dialect = column.getTableMetaData().getDatabaseMetaData().getDialect();

        List<Object> value = BoostTermTypeMapper.convertList(column, termValue.getOld());

        List<String> paths = getTreePathByTerm(value)
                .stream()
                .map(path -> path.concat("%"))
                .collect(Collectors.toList());

        termValue.setValue(paths);

        SqlAppender termCondition = new SqlAppender();

        termCondition.add(not ? "not " : "", "exists(select 1 from ", getTableName(), " tmp where tmp.u_id = ", createColumnName(column, tableAlias));
        int len = paths.size();

        if (len > 0) {
            termCondition.add(" and (");
        }
        for (int i = 0; i < len; i++) {
            if (i > 0) {
                termCondition.addSpc("or");
            }
            if (parent) {
                SqlFunction function = dialect.getFunction(SqlFunction.concat);
                String concat;
                if (function == null) {
                    concat = getTableName() + ".path";
                    log.warn("数据库方言未支持concat函数,你可以调用Dialect.installFunction进行设置!");
                } else {
                    concat = function.apply(SqlFunction.Param.of(RenderPhase.where, Arrays.asList("tmp.path", "'%'")));
                }
                termCondition.add("#{", wherePrefix, ".value[", i, "]}", " like ", concat);
            } else {
                termCondition.add("tmp.path like #{", wherePrefix, ".value[", i, "]}");
            }
        }
        if (len > 0) {
            termCondition.add(")");
        }
        termCondition.add(")");

        return termCondition;
    }
}
