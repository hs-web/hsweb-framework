package org.hswebframework.web.system.authorization.api.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.hswebframework.web.api.crud.entity.Entity;
import org.hswebframework.web.i18n.SingleI18nSupportEntity;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "action")
public class ActionEntity implements Entity, SingleI18nSupportEntity {

    @Schema(description = "操作标识,如: add,query")
    private String action;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "说明")
    private String describe;

    @Schema(description = "其他配置")
    private Map<String, Object> properties;

    @Schema(description = "国际化信息")
    private Map<String, String> i18nMessages;
    @Override
    public Map<String, String> getI18nMessages(String key) {
        return i18nMessages;
    }

}
