package org.hswebframework.web.authorization.dimension;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class DimensionUserBind {
     private String userId;

     private String dimensionType;

     private String dimensionId;

}
