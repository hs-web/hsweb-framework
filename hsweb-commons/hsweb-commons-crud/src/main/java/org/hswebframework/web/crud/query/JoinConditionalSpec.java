package org.hswebframework.web.crud.query;

import org.hswebframework.ezorm.core.Conditional;

public interface JoinConditionalSpec<C extends JoinConditionalSpec<C>> extends JoinOnSpec<C>, Conditional<C> {

    @Override
    JoinNestConditionalSpec<C> nest();

    @Override
    JoinNestConditionalSpec<C> orNest();

    /**
     * 定义join表别名，在后续列转换和条件中可以使用别名进行引用。
     *
     * @param alias 别名
     * @return this
     */
    C alias(String alias);


}
