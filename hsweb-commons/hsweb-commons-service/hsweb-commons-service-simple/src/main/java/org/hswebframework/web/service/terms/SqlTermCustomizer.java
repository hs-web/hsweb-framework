package org.hswebframework.web.service.terms;


import org.hswebframework.ezorm.rdb.operator.builder.fragments.TermFragmentBuilder;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
public interface SqlTermCustomizer extends TermFragmentBuilder {

    String getTermType();

}
