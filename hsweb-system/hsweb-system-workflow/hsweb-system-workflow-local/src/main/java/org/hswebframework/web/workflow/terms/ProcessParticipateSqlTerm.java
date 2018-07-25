package org.hswebframework.web.workflow.terms;

import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.ezorm.rdb.meta.RDBColumnMetaData;
import org.hswebframework.ezorm.rdb.render.SqlAppender;
import org.hswebframework.ezorm.rdb.render.dialect.term.BoostTermTypeMapper;
import org.hswebframework.web.dao.mybatis.mapper.AbstractSqlTermCustomer;
import org.hswebframework.web.dao.mybatis.mapper.ChangedTermValue;

import java.util.List;

/**
 * 参与的任务查询条件
 *
 * @author zhouhao
 * @since 3.0.0-RC
 */
public class ProcessParticipateSqlTerm extends AbstractSqlTermCustomer {
    public ProcessParticipateSqlTerm(String termType) {
        super(termType);
    }

    @Override
    public SqlAppender accept(String wherePrefix, Term term, RDBColumnMetaData column, String tableAlias) {
        ChangedTermValue termValue = createChangedTermValue(term);
        RDBColumnMetaData processInstanceId = column.getTableMetaData().findColumn("processInstanceId");
        if (processInstanceId == null) {
            throw new UnsupportedOperationException("未获取到属性:[processInstanceId]对应的列");
        }
        List<Object> val = BoostTermTypeMapper.convertList(column, termValue.getOld());

        termValue.setValue(val);
        SqlAppender appender = new SqlAppender();

        appender.add("exists(select 1 from ACT_HI_IDENTITYLINK I  WHERE ",
                createColumnName(processInstanceId, tableAlias),
                "=I.PROC_INST_ID_"
                , (!term.getOptions().isEmpty() ? " and I.TYPE_ =" + wherePrefix + ".options[0]" : "")
                , " and I.USER_ID_ ");
        appendCondition(val, wherePrefix, appender);
        appender.add(")");

        return appender;
    }
}
