package org.hswebframework.web.dict;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * 枚举字典,使用枚举来实现数据字典,可通过集成此接口来实现一些有趣的功能.
 * ⚠️:如果使用了位运算来判断枚举,枚举数量不要超过64个,且顺序不要随意变动!
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
     * {@link Enum#ordinal}
     *
     * @return 枚举序号, 如果枚举顺序改变, 此值将被变动
     */
    int ordinal();

    default long getBit() {
        return 1L << (long) ordinal();
    }

    /**
     * 对比是否和value相等,对比地址,值,value转为string忽略大小写对比,text忽略大小写对比
     *
     * @param v value
     * @return 是否相等
     */
    default boolean eq(Object v) {
        return this == v
                || getValue() == v
                || getValue().equals(v)
                || v.equals(getBit())
                || String.valueOf(getValue()).equalsIgnoreCase(String.valueOf(v))
                || getText().equalsIgnoreCase(String.valueOf(v));
    }

    default boolean in(long bit) {
        return (bit & getBit()) != 0;
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
    static <T extends EnumDict> Optional<T> find(Class<T> type, Predicate<T> predicate) {
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
    static <T extends EnumDict<?>> Optional<T> findByValue(Class<T> type, Object value) {
        return find(type, e -> e.getValue() == value || e.getValue().equals(value) || String.valueOf(e.getValue()).equalsIgnoreCase(String.valueOf(value)));
    }

    /**
     * 根据枚举的{@link EnumDict#getText()} 来查找.
     *
     * @see this#find(Class, Predicate)
     */
    static <T extends EnumDict> Optional<T> findByText(Class<T> type, String text) {
        return find(type, e -> e.getText().equalsIgnoreCase(text));
    }

    /**
     * 根据枚举的{@link EnumDict#getValue()},{@link EnumDict#getText()}来查找.
     *
     * @see this#find(Class, Predicate)
     */
    static <T extends EnumDict> Optional<T> find(Class<T> type, Object target) {
        return find(type, v -> v.eq(target));
    }

    @SafeVarargs
    static <T extends EnumDict> long toBit(T... t) {
        long value = 0;
        for (T t1 : t) {
            value |= t1.getBit();
        }
        return value;
    }

    @SafeVarargs
    static <T extends EnumDict> boolean bitIn(long bit, T... t) {
        long value = toBit(t);
        return (bit & value) == value;
    }

    @SafeVarargs
    static <T extends EnumDict> boolean bitInAny(long bit, T... t) {
        long value = toBit(t);
        return (bit & value) != 0;
    }

    static <T extends EnumDict> List<T> getByBit(Class<T> tClass, long bit) {
        List<T> arr = new ArrayList<>();
        for (T t : tClass.getEnumConstants()) {
            if (t.in(bit)) {
                arr.add(t);
            }
        }
        return arr;
    }
}
