package org.hswebframework.web.service;

import lombok.*;
import org.hibernate.validator.constraints.NotBlank;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;
import org.hswebframework.web.commons.entity.SimpleTreeSortSupportEntity;
import org.hswebframework.web.validator.LogicPrimaryKey;
import org.hswebframework.web.validator.group.CreateGroup;

import java.util.List;

/**
 * @author zhouhao
 * @since 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestEntity extends SimpleTreeSortSupportEntity<String> {

    @NotBlank(groups = CreateGroup.class)
    private String name;

    private Byte age;

    private Boolean enabled;

    private List<TestEntity> children;
}
