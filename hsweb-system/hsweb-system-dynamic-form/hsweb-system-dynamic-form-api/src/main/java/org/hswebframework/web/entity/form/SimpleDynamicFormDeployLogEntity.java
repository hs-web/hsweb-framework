package org.hswebframework.web.entity.form;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.ezorm.rdb.mapping.annotation.ColumnType;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.JDBCType;

/**
 * 表单发布日志
 *
 * @author hsweb-generator-online
 */
@Table(name = "s_dyn_form_log")
@Getter
@Setter
public class SimpleDynamicFormDeployLogEntity extends SimpleGenericEntity<String> implements DynamicFormDeployLogEntity {
    //表单ID
    @Column(name = "form_id", length = 32)
    private String formId;
    //发布的版本
    @Column
    private Long version;
    //发布时间
    @Column(name = "deploy_time")
    private Long deployTime;
    //部署的元数据
    @Column(name = "meta_data")
    @ColumnType(jdbcType = JDBCType.LONGVARCHAR)
    private String metaData;
    //部署状态
    @Column
    private Byte status;

    @Override
    @Id
    @Column(name = "u_id")
    public String getId() {
        return super.getId();
    }
}