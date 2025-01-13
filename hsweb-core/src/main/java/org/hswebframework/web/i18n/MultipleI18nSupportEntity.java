package org.hswebframework.web.i18n;

import org.apache.commons.collections4.MapUtils;

import java.util.Collections;
import java.util.Map;

/**
 * 支持多个国际化信息的实体类,用于多个字段的国际化支持.
 *
 * @author zhouhao
 * @since 4.0.18
 */
public interface MultipleI18nSupportEntity extends I18nSupportEntity {

    /**
     * 全部国际化信息,key为字段名,value为国际化信息.
     * <pre>{@code
     *  {
     *      "name":{"zh":"中文","en":"english"},
     *      "desc":{"zh":"描述","en":"description"}
     *  }
     * }</pre>
     *
     * @return 国际化信息
     */
    Map<String, Map<String, String>> getI18nMessages();

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
    @Override
    default Map<String, String> getI18nMessages(String key) {
        Map<String, Map<String, String>> source = getI18nMessages();
        if (MapUtils.isEmpty(source)) {
            return Collections.emptyMap();
        }
        return source.get(key);
    }
}
