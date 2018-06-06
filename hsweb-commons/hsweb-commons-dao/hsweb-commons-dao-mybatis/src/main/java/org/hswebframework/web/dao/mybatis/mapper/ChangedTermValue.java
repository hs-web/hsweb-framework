package org.hswebframework.web.dao.mybatis.mapper;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@Data
@AllArgsConstructor
public class ChangedTermValue {
    private Object old;

    private Object value;
}
