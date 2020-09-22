package org.hswebframework.web.system.authorization.defaults.webflux;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hswebframework.web.authorization.DimensionType;

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
        return of(type.getId(), type.getName());
    }
}
