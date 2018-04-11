package org.hswebframework.web.dict;

import org.hswebframework.web.dict.defaults.DefaultDictParser;

import java.lang.annotation.*;

/**
 * @author zhouhao
 * @since 3.0
 */
@Target({ElementType.METHOD, ElementType.FIELD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Dict {
    /**
     * 如果id对应的字典已经存在,则设置此属性进行关联,除了{@link Dict#parserId}属性的其他属性将被忽略<br>
     * 如果id对应的字典不存在,则将使用其他属性进行定义
     *
     * @return 字典ID
     * @see DictDefine#getId()
     * @see DictDefineRepository
     */
    String id() default "";

    /**
     * 字典别名,如果指定了别名:
     * <ul>
     * <li>在序列化为json后,会添加一个字段到json中.
     * 字段的名称为此属性的值,字段的值为{@link Dict#parserId}对应的解析器的解析结果</li>
     * <li>在反序列化json时,如果被注解的字段值为null,将尝试解析别名字段的值并设置到注解的字段</li>
     * </ul>
     *
     * @return 别名
     */
    String alias() default "";

    /**
     * @return 字典说明, 备注
     */
    String comments() default "";

    /**
     * 字典解析器ID
     *
     * @return 字典解析器的id, 默认为default, 如果对应的解析器不存在,也将使用default
     * @see DictParser
     * @see DefaultDictParser
     */
    String parserId() default "default";

    /**
     * 如果要直接在注解上定义字典,通过设置此属性进行定义
     *
     * @return 字典选项
     */
    Item[] items() default {};
}
