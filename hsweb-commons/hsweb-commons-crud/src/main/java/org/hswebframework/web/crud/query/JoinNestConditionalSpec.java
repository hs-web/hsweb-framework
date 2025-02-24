package org.hswebframework.web.crud.query;

import org.hswebframework.ezorm.core.NestConditional;
import org.hswebframework.ezorm.core.TermTypeConditionalSupport;

public interface JoinNestConditionalSpec<C extends TermTypeConditionalSupport>
        extends JoinOnSpec<JoinNestConditionalSpec<C>>, NestConditional<C> {

    @Override
    JoinNestConditionalSpec<JoinNestConditionalSpec<C>> nest();

    @Override
    JoinNestConditionalSpec<JoinNestConditionalSpec<C>> orNest();


}
