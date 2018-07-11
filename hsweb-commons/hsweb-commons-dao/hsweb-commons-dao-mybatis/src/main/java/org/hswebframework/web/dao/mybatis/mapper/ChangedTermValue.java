package org.hswebframework.web.dao.mybatis.mapper;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@Data
@AllArgsConstructor
public class ChangedTermValue implements Serializable {
    private static final long serialVersionUID = 6373611532663483048L;

    private Object old;

    private Object value;
}
