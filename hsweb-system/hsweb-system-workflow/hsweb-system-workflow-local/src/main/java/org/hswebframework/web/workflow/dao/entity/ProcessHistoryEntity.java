package org.hswebframework.web.workflow.dao.entity;

import lombok.*;
import org.hibernate.validator.constraints.NotBlank;
import org.hswebframework.ezorm.rdb.mapping.annotation.ColumnType;
import org.hswebframework.ezorm.rdb.mapping.annotation.JsonCodec;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.sql.JDBCType;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "s_wf_proc_his")
public class ProcessHistoryEntity extends SimpleGenericEntity<String> {

    private static final long serialVersionUID = 1413072102870937450L;

    @NotBlank(message = "[type]不能为空")
    @Column
    private String type;

    @NotBlank(message = "[typeText]不能为空")
    @Column(name = "type_text")
    private String typeText;

    @NotBlank(message = "[processDefineId]不能为空")
    @Column(name = "proc_def_id",length = 64)
    private String processDefineId;

    @NotBlank(message = "[processInstanceId]不能为空")
    @Column(name = "proc_ins_id",length = 64)
    private String processInstanceId;

    @NotBlank(message = "[businessKey]不能为空")
    @Column(name = "biz_key",length = 64)
    private String businessKey;

    @NotNull(message = "[createTime]不能为空")
    @Column(name = "create_time")
    private Date createTime;

    @NotBlank(message = "[creatorId]不能为空")
    @Column(name = "creator_id",length = 32)
    private String creatorId;

    @NotBlank(message = "[creatorName]不能为空")
    @Column(name = "creator_name",length = 64)
    private String creatorName;

    @Column(name = "biz_data")
    @JsonCodec
    @ColumnType(jdbcType = JDBCType.LONGNVARCHAR)
    private Map<String, Object> data = new HashMap<>();

    @Column(name = "task_id",length = 64)
    private String taskId;

    @Column(name = "task_def_key",length = 64)
    private String taskDefineKey;

    @Column(name = "task_name")
    private String taskName;

    @Override
    @Id
    @Column(name = "u_id")
    public String getId() {
        return super.getId();
    }
}
