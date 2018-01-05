package org.hswebframework.web.organizational.authorization.simple;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hswebframework.web.organizational.authorization.District;

/**
 * @author zhouhao
 * @since 3.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimpleDistrict implements District {
    private String id;
    private String name;
    private String fullName;
    private String code;
}
