package org.hswebframework.web.dict;

/**
 * 国际化支持的枚举数据字典,自动根据 : <b>类名.name()</b>来获取text.如果没有定义则获取{@link EnumDict#getText()}的值.
 * 例:
 * 定义枚举并实现{@link I18nEnumDict}接口
 * <pre>
 * package com.domain.dict;
 *
 * &#64;AllArgsConstructor
 * &#64;Getter
 * &#64;Dict("device-state")
 * public enum DeviceState implements I18nEnumDict&lt;String&gt; {
 *     notActive("未启用"),
 *     offline("离线"),
 *     online("在线");
 *
 *     private final String text;
 *
 *     &#64;Override
 *     public String getValue() {
 *         return name();
 *     }
 *   }
 * </pre>
 * <p>
 * 在resources下添加文件: <code>i18n/{path}/{name}_zh_CN.properties</code>
 * <p>
 * 注意: {path}修改为自己的名称。{name}不能包含下划线(_)。不能存在完全重名的文件。
 * <p>
 * 正确的格式: i18n/my-module/messages_zh_CN.properties
 * <p>
 * 错误的格式: i18n/my-module/messages_msg_zh_CN.properties
 * <p>
 * 文件内容:
 * <pre>
 * com.domain.dict.DeviceState.notActive=未启用
 * com.domain.dict.DeviceState.offline=离线
 * com.domain.dict.DeviceState.online=在线
 * </pre>
 *
 * @param <V> 值类型
 * @author zhouhao
 * @since 4.0.11
 */
public interface I18nEnumDict<V> extends EnumDict<V> {

    /**
     * 枚举name
     *
     * @return name
     * @see Enum#name()
     */
    String name();

    @Override
    default String getI18nCode() {
        return this.getClass().getName() + "." + name();
    }
}
