package org.hswebframework.web.dashboard;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.NotBlank;
import org.hswebframework.web.commons.entity.RecordCreationEntity;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;
import org.hswebframework.web.validator.group.CreateGroup;

import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
public class DashBoardConfigEntity extends SimpleGenericEntity<String> implements RecordCreationEntity,Comparable<DashBoardConfigEntity> {

    private static final long serialVersionUID = 3911748291957287662L;

    @NotBlank(groups = CreateGroup.class)
    private String name;

    @NotBlank(groups = CreateGroup.class)
    private String type;

    private String template;

    private String script;

    private String scriptLanguage;

    private String permission;

    private Boolean defaultConfig;

    private Byte status;

    @NotBlank(groups = CreateGroup.class)
    private String creatorId;

    @NotNull(groups = CreateGroup.class)
    private Long createTime;

    private Long sortIndex;

    @Override
    public int compareTo(DashBoardConfigEntity o) {
        if(sortIndex==null){
            return 0;
        }
        if(o.sortIndex==null){
            return 1;
        }
        return Long.compare(sortIndex,o.sortIndex);
    }
}
