package org.hswebframework.web.dao.crud;

import lombok.Data;

import javax.persistence.Column;

/**
 *
 * @author zhouhao
 * @since
 */
@Data
public class NestEntity {

    @Column
    private String name;
}
