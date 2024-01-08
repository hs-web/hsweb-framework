package org.hswebframework.web.authorization.dimension;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hswebframework.web.authorization.Dimension;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class DimensionUserDetail implements Serializable {
    private static final long serialVersionUID = -6849794470754667710L;


    private String userId;

    private List<Dimension> dimensions;

    public DimensionUserDetail merge(DimensionUserDetail detail) {
        DimensionUserDetail newDetail = new DimensionUserDetail();
        newDetail.setUserId(userId);
        newDetail.setDimensions(new ArrayList<>());
        if (null != dimensions) {
            newDetail.dimensions.addAll(dimensions);
        }
        if (null != detail.getDimensions()) {
            newDetail.dimensions.addAll(detail.getDimensions());
        }
        return newDetail;
    }
}
