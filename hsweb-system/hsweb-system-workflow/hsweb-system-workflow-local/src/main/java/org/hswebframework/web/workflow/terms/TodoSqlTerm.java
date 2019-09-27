package org.hswebframework.web.workflow.terms;

import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.ezorm.rdb.metadata.RDBColumnMetadata;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.PrepareSqlFragments;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.SqlFragments;
import org.hswebframework.web.service.terms.AbstractSqlTermCustomizer;

import java.util.List;

/**
 * 代办的任务查询条件
 *
 * @author zhouhao
 * @since 3.0.0-RC
 */
public class TodoSqlTerm extends AbstractSqlTermCustomizer {
    public TodoSqlTerm(String termType) {
        super(termType);
    }


    @Override
    public SqlFragments createFragments(String columnFullName, RDBColumnMetadata column, Term term) {
        PrepareSqlFragments fragments = PrepareSqlFragments.of();

        List<Object> val = convertList(term.getValue());
        fragments.addSql("exists(select 1 from ACT_RU_TASK RES WHERE ",
                columnFullName,
                "= RES.PROC_INST_ID_ and RES.ASSIGNEE_ and ", columnFullName);

        appendCondition(fragments, column, val);
        return fragments;
    }

    @Override
    public String getName() {
        return "查询代办数据";
    }
}
