package org.hswebframework.web.crud.sql.terms;

import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.ezorm.rdb.metadata.RDBColumnMetadata;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.PrepareSqlFragments;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.SqlFragments;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.term.AbstractTermFragmentBuilder;

import java.util.Arrays;
import java.util.List;

/**
 * 树结构相关数据查询条件构造器,用于构造根据树结构数据以及子节点查询相关联的数据,
 * 如查询某个地区以及下级地区的数据.
 *
 * @author zhouhao
 * @since 4.0.17
 */
public abstract class TreeChildTermBuilder extends AbstractTermFragmentBuilder {
    public TreeChildTermBuilder(String termType, String name) {
        super(termType, name);
    }

    protected abstract String tableName();

    @Override
    public SqlFragments createFragments(String columnFullName, RDBColumnMetadata column, Term term) {
        List<Object> id = convertList(column, term);

        String tableName = getTableName(tableName(), column);

        String[] args = new String[id.size()];
        Arrays.fill(args, "?");

        RDBColumnMetadata pathColumn = column
            .getOwner()
            .getColumn("path")
            .orElseThrow(() -> new IllegalArgumentException("not found 'path' column"));

        RDBColumnMetadata idColumn = column
            .getOwner()
            .getColumn("id")
            .orElseThrow(() -> new IllegalArgumentException("not found 'id' column"));

        return PrepareSqlFragments
            .of().addSql(
                "exists(select 1 from", tableName, "_p join", tableName,
                "_c on", idColumn.getFullName("_c"), "in(", String.join("?", args), ")",
                "and", pathColumn.getFullName("_p"), "like concat(" + pathColumn.getFullName("_c") + ",'%')",
                "where", columnFullName, "=", idColumn.getFullName("_p"), ")"
            )
            .addParameter(id);

    }
}
