package org.hswebframework.web.dict.defaults;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hswebframework.web.dict.ItemDefine;
import org.hswebframework.web.i18n.MultipleI18nSupportEntity;

import java.util.Locale;
import java.util.Map;

/**
 * @author zhouhao
 * @since 3.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefaultItemDefine implements ItemDefine, MultipleI18nSupportEntity {
    private static final long serialVersionUID = 1L;
    
    private String text;
    private String value;
    private String comments;
    private int ordinal;
    private Map<String, Map<String, String>> i18nMessages;
    
    public DefaultItemDefine(String text, String value, String comments, int ordinal) {
        this.text = text;
        this.value = value;
        this.comments = comments;
        this.ordinal = ordinal;
    }
    
    @Override
    public String getI18nMessage(Locale locale) {
        return getI18nMessage("text", locale, text);
    }
}
