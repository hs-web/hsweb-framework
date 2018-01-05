package org.hswebframework.web.organizational.authorization.simple;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hswebframework.web.organizational.authorization.Department;
import org.hswebframework.web.organizational.authorization.Organization;

/**
 * @author zhouhao
 * @since 3.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimpleDepartment implements Department {
    private String id;
    private String name;
    private String code;
    private Organization org;
}
