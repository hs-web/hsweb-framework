package org.hswebframework.web.system.authorization.api.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.hswebframework.ezorm.rdb.mapping.annotation.ColumnType;
import org.hswebframework.ezorm.rdb.mapping.annotation.JsonCodec;
import org.hswebframework.web.api.crud.entity.Entity;
import org.hswebframework.web.i18n.MultipleI18nSupportEntity;
import org.hswebframework.web.i18n.SingleI18nSupportEntity;

import javax.persistence.Column;
import java.sql.JDBCType;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "action")
public class ActionEntity implements Entity, MultipleI18nSupportEntity {

    @Schema(description = "操作标识,如: add,query")
    private String action;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "说明")
    private String describe;

    @Schema(description = "其他配置")
    private Map<String, Object> properties;

    @Schema(description = "国际化信息")
    private Map<String, Map<String, String>> i18nMessages;

    public String getI18nName() {
        return getI18nMessage("name", name);
    }
    public String getI18nDescribe() {
        return getI18nMessage("describe", describe);
    }
}
