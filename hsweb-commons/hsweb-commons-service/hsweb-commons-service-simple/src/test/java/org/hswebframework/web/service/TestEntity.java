package org.hswebframework.web.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hswebframework.web.commons.entity.SimpleTreeSortSupportEntity;
import org.hswebframework.web.validator.group.CreateGroup;

import javax.validation.constraints.NotBlank;
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
public class TestEntity extends SimpleTreeSortSupportEntity<String>
//        implements RecordModifierEntity
{

    @NotBlank(groups = CreateGroup.class)
    private String name;

    private Byte age;

    private Boolean enabled;

    private List<TestEntity> children;

    private String modifierId;

    private Long modifyTime;

}
