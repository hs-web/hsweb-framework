package org.hswebframework.web.dict;

/**
 * 国际化支持的枚举数据字典,自动根据 : 类名.name()来获取text
 *
 * @param <V>
 */
public interface I18nEnumDict<V> extends EnumDict<V> {

    String name();

    @Override
    default String getI18nCode() {
        return this.getClass().getName() + "." + name();
    }
}
