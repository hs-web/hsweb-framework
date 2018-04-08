package org.hswebframework.web.dao.mybatis.builder.jpa;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 * @since
 */
@Getter
@Setter
public class AbstractEntity {

    @Column(name = "id")
    private Long id;
}
