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
 * 动态表单
 *
 * @author hsweb-generator-online
 */
@Getter
@Setter
@Table(name = "s_dyn_form")
public class SimpleDynamicFormEntity extends SimpleGenericEntity<String> implements DynamicFormEntity {
    //表单名称
    @Column
    private String name;
    //数据库名
    @Column(name = "db_name")
    private String databaseName;
    //数据库表名
    @Column(name = "t_name")
    private String databaseTableName;
    //备注
    @Column
    private String describe;
    //版本
    @Column
    private Long version;
    //创建人id
    @Column(name = "creator_id")
    private String creatorId;
    //创建时间
    @Column(name = "creator_time")
    private Long createTime;
    //修改时间
    @Column(name = "update_time")
    private Long updateTime;
    //是否已发布
    @Column(name = "is_deployed")
    private Boolean deployed;
    //别名
    @Column
    private String alias;
    //触发器
    @Column
    @ColumnType(jdbcType = JDBCType.LONGNVARCHAR)
    private String triggers;
    //表链接
    @Column
    @ColumnType(jdbcType = JDBCType.LONGNVARCHAR)
    private String correlations;
    //数据源id,为空使用默认数据源
    @Column(name = "data_source_id")
    @ColumnType(jdbcType = JDBCType.LONGNVARCHAR)
    private String dataSourceId;
    //表单类型
    @Column
    private String type;
    @Column(length = 1024)
    private String tags;

    @Override
    @Id
    @Column(name = "u_id")
    public String getId() {
        return super.getId();
    }

}