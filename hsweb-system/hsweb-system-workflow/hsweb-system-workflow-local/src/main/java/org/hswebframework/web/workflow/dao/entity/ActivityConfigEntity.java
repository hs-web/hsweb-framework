package org.hswebframework.web.workflow.dao.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;
import org.hswebframework.ezorm.rdb.mapping.annotation.ColumnType;
import org.hswebframework.ezorm.rdb.mapping.annotation.JsonCodec;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;
import org.hswebframework.web.validator.group.CreateGroup;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.sql.JDBCType;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Table(name = "s_wf_act_conf", indexes = {
        @Index(name = "idx_wf_act_conf_act_id", columnList = "act_id"),
        @Index(name = "idx_wf_act_conf_def_id", columnList = "proc_def_id")
})
public class ActivityConfigEntity extends SimpleGenericEntity<String> {

    private static final long serialVersionUID = 2909584456889604626L;

    /**
     * 节点ID
     */
    @NotBlank(groups = CreateGroup.class)
    @Column(name = "act_id")
    private String activityId;

    /**
     * 流程定义key
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
    @Column(name = "form_id")
    private String formId;

    /**
     * 节点办理候选人维度,用于设置该环节的办理人,json格式,由CandidateDimensionParser解析
     */
    @Column(name = "candidate_dimension")
    @ColumnType(jdbcType = JDBCType.LONGNVARCHAR)
    private String candidateDimension;

    @NotNull(groups = CreateGroup.class)
    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "create_time")
    private Date updateTime;

    @NotNull(groups = CreateGroup.class)
    @Column
    private Byte status;

    @Column
    @ColumnType(jdbcType = JDBCType.LONGNVARCHAR)
    @JsonCodec
    private List<ListenerConfig> listeners;

    @Override
    @Id
    @Column(name = "u_id")
    public String getId() {
        return super.getId();
    }
}
