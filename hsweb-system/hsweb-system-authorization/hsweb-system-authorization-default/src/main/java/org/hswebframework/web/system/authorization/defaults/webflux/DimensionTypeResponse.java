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
        DimensionTypeResponse response = FastBeanCopier.copy(type,
            EntityFactoryHolder.newInstance(DimensionTypeResponse.class, DimensionTypeResponse::new));
        // 类型接口允许枚举和只读实现；必选字段不依赖属性复制器的可写 Bean 属性。
        response.setId(type.getId());
        response.setName(type.getName());
        return response;
    }
}
