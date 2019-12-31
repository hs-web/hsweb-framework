package org.hswebframework.web.dao.crud;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;

/**
 *
 * @author zhouhao
 * @since
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NestEntity {

    @Column
    private String name;
}
