package org.hswebframework.web.validator;


import java.lang.annotation.*;

/**
 * 逻辑主键,用于在新增或者修改前进行重复数据判断.
 * 可在类或者字段上进行注解
 *
 * @see DuplicateKeyException
 * @since 3.0.0-RC
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogicPrimaryKey {

    /**
     * 属性名称,如果注解在类上,值为空的时候必须指定{@link this#groups()},并且group对应的类上必须有合法的LogicPrimaryKey注解
     *
     * @return 属性名集合, 在字段上此属性无效
     */
    String[] value() default {};

    /**
     * 验证条件,值为一个spel表达式,可在验证的时候根据实体中不同的属性,使用不同的规则,例如:
     * <pre>
     *     #object.name!=null
     * </pre>
     *
     * @return spel表达式, 为空时不进行判断
     */
    String condition() default "";

    /**
     * 查询条件类型,基于hsweb-commons-dao中的termType,可指定此字段查询的方式,默认为:'eq'(=)
     *
     * @return 自定义查询条件类型
     */
    String termType() default "";

    /**
     * 是否匹配空值,如果为true,并且值为<code>null</code>时,进行where field is null,如果值为<code>""</code>,则进行where field =''。否则将跳过该字段验证
     *
     * @return 是否匹配空值
     */
    boolean matchNullOrEmpty() default false;

    /**
     * 验证分组,如果在验证的时候指定了分组,则只会验证对应分组的规则
     *
     * @return 分组
     */
    Class[] groups() default Void.class;

}
