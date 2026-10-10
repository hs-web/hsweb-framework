package org.hswebframework.web.system.authorization.defaults.webflux;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hswebframework.web.authorization.DimensionType;
import org.hswebframework.web.api.crud.entity.EntityFactoryHolder;
import org.hswebframework.web.bean.FastBeanCopier;

@Getter
@Setter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class DimensionTypeResponse {

    @Schema(description = "类型ID")
    private String id;

    @Schema(description = "类型名称")
    private String name;

    public static DimensionTypeResponse of(DimensionType type) {
        return FastBeanCopier.copy(type, EntityFactoryHolder.newInstance(DimensionTypeResponse.class, DimensionTypeResponse::new));
    }
}
