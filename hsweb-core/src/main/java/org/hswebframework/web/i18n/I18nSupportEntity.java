package org.hswebframework.web.i18n;

import org.apache.commons.collections4.MapUtils;

import java.util.Locale;
import java.util.Map;

/**
 * 国际化支持实体,实现此接口,提供基础的国际化支持.如：针对实体类某些字段的国际化支持.
 *
 * @author zhouhao
 * @since 4.0.18
 */
public interface I18nSupportEntity {

    /**
     * 根据key获取全部国际化信息,key为地区标识,value为国际化消息.
     * <pre>{@code
     *
     *    {"zh":"你好","en":"hello"}
     *
     *  }</pre>
     *
     * @param key key
     * @return 国际化信息
     */
    Map<String, String> getI18nMessages(String key);

    /**
     * 根据当前地区获取,指定key的国际化信息.
     * <pre>{@code
     *
     *    public String getI18nName(){
     *        return getI18nMessages("name",this.name);
     *    }
     *
     * }</pre>
     *
     * @param key key
     * @return 国际化信息
     * @see LocaleUtils#transform
     */
    default String getI18nMessage(String key, String defaultMessage) {
        return getI18nMessage(key, LocaleUtils.current(), defaultMessage);
    }

    /**
     * 根据指定的语言地区,获取指定key的国际化信息.
     * <pre>{@code
     *
     *    public String getI18nName(){
     *        return getI18nMessages("name",Locale.US,this.name);
     *    }
     *
     * }</pre>
     *
     * @param key key
     * @return 国际化信息
     */
    default String getI18nMessage(String key, Locale locale, String defaultMessage) {

        Map<String, String> entries = getI18nMessages(key);

        if (MapUtils.isEmpty(entries)) {
            return defaultMessage;
        }

        return LocaleUtils.getMessage(entries::get, locale, () -> defaultMessage);
    }

}
