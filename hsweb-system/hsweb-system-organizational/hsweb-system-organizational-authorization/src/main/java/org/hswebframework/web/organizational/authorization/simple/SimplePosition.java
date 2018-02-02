package org.hswebframework.web.organizational.authorization.simple;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hswebframework.web.organizational.authorization.Department;
import org.hswebframework.web.organizational.authorization.Position;

/**
 * @author zhouhao
 * @since 3.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimplePosition implements Position {
    private static final long serialVersionUID = 1_0;
    private String id;

    private String name;

    private String code;

    private Department department;
}
