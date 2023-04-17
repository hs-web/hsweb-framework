package org.hswebframework.web.crud.query;

import org.hswebframework.ezorm.core.NestConditional;
import org.hswebframework.ezorm.core.StaticMethodReferenceColumn;
import org.hswebframework.ezorm.core.TermTypeConditionalSupport;
import org.hswebframework.ezorm.core.param.TermType;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.term.AbstractTermFragmentBuilder;

public interface JoinNestConditionalSpec<C extends TermTypeConditionalSupport>
        extends JoinOnSpec<C>, NestConditional<C> {

    @Override
    JoinNestConditionalSpec<NestConditional<C>> nest();

    @Override
    JoinNestConditionalSpec<NestConditional<C>> orNest();


}
