package org.hswebframework.web.service;

import lombok.*;
import org.hibernate.validator.constraints.NotBlank;
import org.hswebframework.web.commons.entity.RecordModifierEntity;
import org.hswebframework.web.commons.entity.SimpleTreeSortSupportEntity;
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
public class TestModifyEntity extends SimpleTreeSortSupportEntity<String>
        implements RecordModifierEntity
{

    @NotBlank(groups = CreateGroup.class)
    private String name;

    private Byte age;

    private Boolean enabled;

    private List<TestModifyEntity> children;

    private String modifierId;

    private Long modifyTime;

}
