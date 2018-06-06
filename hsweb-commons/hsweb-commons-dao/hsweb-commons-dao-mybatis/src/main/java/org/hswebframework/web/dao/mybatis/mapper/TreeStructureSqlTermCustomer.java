package org.hswebframework.web.dao.mybatis.mapper;

import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.ezorm.rdb.meta.RDBColumnMetaData;
import org.hswebframework.ezorm.rdb.render.SqlAppender;
import org.hswebframework.ezorm.rdb.render.dialect.term.BoostTermTypeMapper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
public abstract class TreeStructureSqlTermCustomer extends AbstractSqlTermCustomer {
    boolean not = false;

    public TreeStructureSqlTermCustomer(String termType, boolean not) {
        super(termType);
        this.not = not;
    }

    protected abstract String getTableName();

    protected abstract List<String> getTreePathByTerm(List<Object> termValue);

    @Override
    public SqlAppender accept(String wherePrefix, Term term, RDBColumnMetaData column, String tableAlias) {
        List<Object> value = BoostTermTypeMapper.convertList(column, term.getValue());
        List<String> paths = getTreePathByTerm(value)
                .stream()
                .map(path -> path.concat("%"))
                .collect(Collectors.toList());

        term.setValue(paths);
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
            termCondition.add("tmp.path like #{", wherePrefix, ".value[", i, "]}");
        }
        if (len > 0) {
            termCondition.add(")");
        }
        termCondition.add(")");

        return termCondition;
    }
}
