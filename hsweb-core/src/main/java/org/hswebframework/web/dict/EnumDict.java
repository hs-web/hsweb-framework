package org.hswebframework.web.dict;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * 枚举字典,使用枚举来实现数据字典,可通过集成此接口来实现一些有趣的功能
 *
 * @author zhouhao
 * @see 3.0
 */
public interface EnumDict<V> {
    /**
     * 枚举选项的值,通常由字母或者数字组成,并且在同一个枚举中值唯一;对应数据库中的值通常也为此值
     *
     * @return 枚举的值
     * @see ItemDefine#getValue()
     */
    V getValue();

    /**
     * 枚举字典选项的文本,通常为中文
     *
     * @return 枚举的文本
     * @see ItemDefine#getText()
     */
    String getText();

    /**
     * 对比是否和value相等,对比地址,值,value转为string忽略大小写对比,text忽略大小写对比
     *
     * @param v value
     * @return 是否相等
     */
    default boolean eq(Object v) {
        return getValue() == v
                || getValue().equals(v)
                || String.valueOf(getValue()).equalsIgnoreCase(String.valueOf(v))
                || getText().equalsIgnoreCase(String.valueOf(v));
    }


    /**
     * 枚举选项的描述,对一个选项进行详细的描述有时候是必要的.默认值为{@link this#getText()}
     *
     * @return 描述
     */
    default String getComments() {
        return getText();
    }

    /**
     * 从指定的枚举类中查找想要的枚举,并返回一个{@link Optional},如果未找到,则返回一个{@link Optional#empty()}
     *
     * @param type      实现了{@link EnumDict}的枚举类
     * @param predicate 判断逻辑
     * @param <T>       枚举类型
     * @return 查找到的结果
     */
    static <T extends Enum & EnumDict> Optional<T> find(Class<T> type, Predicate<T> predicate) {
        if (type.isEnum()) {
            for (T enumDict : type.getEnumConstants()) {
                if (predicate.test(enumDict)) {
                    return Optional.of(enumDict);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * 根据枚举的{@link EnumDict#getValue()}来查找.
     *
     * @see this#find(Class, Predicate)
     */
    static <T extends Enum & EnumDict<?>> Optional<T> findByValue(Class<T> type, Object value) {
        return find(type, e -> e.getValue() == value || e.getValue().equals(value) || String.valueOf(e.getValue()).equalsIgnoreCase(String.valueOf(value)));
    }

    /**
     * 根据枚举的{@link EnumDict#getText()} 来查找.
     *
     * @see this#find(Class, Predicate)
     */
    static <T extends Enum & EnumDict> Optional<T> findByText(Class<T> type, String text) {
        return find(type, e -> e.getText().equalsIgnoreCase(text));
    }

    /**
     * 根据枚举的{@link EnumDict#getValue()},{@link EnumDict#getText()}来查找.
     *
     * @see this#find(Class, Predicate)
     */
    static <T extends Enum & EnumDict> Optional<T> find(Class<T> type, Object target) {
        return find(type, v->v.eq(target));
    }
}
