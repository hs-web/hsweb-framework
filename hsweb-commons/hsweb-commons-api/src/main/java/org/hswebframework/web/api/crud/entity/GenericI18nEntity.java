package org.hswebframework.web.api.crud.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.MapUtils;
import org.hswebframework.ezorm.rdb.mapping.annotation.ColumnType;
import org.hswebframework.ezorm.rdb.mapping.annotation.JsonCodec;
import org.hswebframework.web.i18n.I18nSupportEntity;

import javax.persistence.Column;
import java.sql.JDBCType;
import java.util.Collections;
import java.util.Map;

@Getter
@Setter
public class GenericI18nEntity<PK> extends GenericEntity<PK> implements I18nSupportEntity {

    /**
     * map key为标识，如: name , description. value为国际化信息
     *
     * <pre>{@code
     *   {
     *       "name":{"zh":"名称","en":"name"},
     *       "description":{"zh":"描述","en":"description"}
     *   }
     * }</pre>
     */
    @Schema(title = "国际化信息定义")
    @Column
    @JsonCodec
    @ColumnType(jdbcType = JDBCType.LONGVARCHAR, javaType = String.class)
    private Map<String, Map<String, String>> i18nMessages;

    @Override
    public Map<String, String> getI18nMessages(String key) {
        if (MapUtils.isEmpty(i18nMessages)) {
            return Collections.emptyMap();
        }
        return i18nMessages.getOrDefault(key, Collections.emptyMap());
    }
}
