package org.hswebframework.web.organizational.authorization.simple;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hswebframework.web.organizational.authorization.Personnel;

/**
 * @author zhouhao
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimplePersonnel implements Personnel {
    private static final long serialVersionUID = 1_0;
    private String id;
    private String name;
    private String phone;
    private String photo;
    private String email;
}
