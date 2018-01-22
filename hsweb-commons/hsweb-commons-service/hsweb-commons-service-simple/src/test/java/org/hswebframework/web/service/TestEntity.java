package org.hswebframework.web.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

/**
 * @author zhouhao
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestEntity extends SimpleGenericEntity<String> {
    private String name;

    private Byte age;

    private Boolean enabled;
}
