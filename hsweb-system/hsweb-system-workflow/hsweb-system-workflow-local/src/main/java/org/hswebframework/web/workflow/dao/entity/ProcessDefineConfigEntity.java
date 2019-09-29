package org.hswebframework.web.workflow.dao.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;
import org.hswebframework.ezorm.rdb.mapping.annotation.ColumnType;
import org.hswebframework.ezorm.rdb.mapping.annotation.JsonCodec;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;
import org.hswebframework.web.validator.group.CreateGroup;

import javax.persistence.Column;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.sql.JDBCType;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Table(name = "s_wf_proc_conf", indexes = {
        @Index(name = "idx_wf_proc_conf_def_id", columnList = "proc_def_id")
})
public class ProcessDefineConfigEntity extends SimpleGenericEntity<String> {

    private static final long serialVersionUID = -140312693789656665L;
    /**
     * 流程定义Key
     */
    @NotBlank(groups = CreateGroup.class)
    @Column(name = "proc_def_key")
    private String processDefineKey;

    /**
     * 流程定义ID
     */
    @NotBlank(groups = CreateGroup.class)
    @Column(name = "proc_def_id")
    private String processDefineId;

    /**
     * 后台表单ID
     */
    @NotBlank(groups = CreateGroup.class)
    @Column(name = "form_id")
    private String formId;

    /**
     * 权限维度,用于控制不同人,可发起不同的流程
     */
    @Column(name = "permission_dimension")
    @ColumnType(jdbcType = JDBCType.LONGNVARCHAR)
    private String permissionDimension;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_time")
    private Date updateTime;

    /**
     * 状态
     */
    @NotNull(groups = CreateGroup.class)
    @Column
    private Byte status;

    @Column
    @JsonCodec
    @ColumnType(jdbcType = JDBCType.LONGVARCHAR)
    private List<ListenerConfig> listeners;

}
