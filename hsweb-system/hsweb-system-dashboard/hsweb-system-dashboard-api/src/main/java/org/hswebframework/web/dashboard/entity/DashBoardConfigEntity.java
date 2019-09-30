package org.hswebframework.web.dashboard.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.NotBlank;
import org.hswebframework.ezorm.rdb.mapping.annotation.ColumnType;
import org.hswebframework.web.commons.entity.RecordCreationEntity;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;
import org.hswebframework.web.validator.group.CreateGroup;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.sql.JDBCType;

@EqualsAndHashCode(callSuper = true)
@Data
@Table(name = "s_dashboard_conf")
public class DashBoardConfigEntity extends SimpleGenericEntity<String> implements RecordCreationEntity, Comparable<DashBoardConfigEntity> {

    private static final long serialVersionUID = 3911748291957287662L;

    @NotBlank(groups = CreateGroup.class)
    @Column
    private String name;

    @NotBlank(groups = CreateGroup.class)
    @Column
    private String type;

    @Column
    @ColumnType(jdbcType = JDBCType.LONGVARCHAR)
    private String template;

    @Column
    @ColumnType(jdbcType = JDBCType.LONGVARCHAR)
    private String script;

    @Column(name = "script_language")
    private String scriptLanguage;

    @Column(name = "permission")
    private String permission;

    @Column(name = "is_default")
    private Boolean defaultConfig;

    @Column
    private Byte status;

    @NotBlank(groups = CreateGroup.class)
    @Column(name = "creator_id")
    private String creatorId;

    @NotNull(groups = CreateGroup.class)
    @Column(name = "create_time")
    private Long createTime;

    @Column(name = "sort_index")
    private Long sortIndex;

    @Override
    public int compareTo(DashBoardConfigEntity o) {
        if (sortIndex == null) {
            return 0;
        }
        if (o.sortIndex == null) {
            return 1;
        }
        return Long.compare(sortIndex, o.sortIndex);
    }

    @Override
    @Id
    @Column(name = "u_id")
    public String getId() {
        return super.getId();
    }
}
