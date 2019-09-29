package org.hswebframework.web.entity.datasource;

import lombok.*;
import org.hswebframework.ezorm.rdb.mapping.annotation.ColumnType;
import org.hswebframework.ezorm.rdb.mapping.annotation.JsonCodec;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.JDBCType;
import java.util.Map;

/**
 * 数据源配置
 *
 * @author hsweb-generator-online
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "s_datasource_conf")
public class SimpleDataSourceConfigEntity extends SimpleGenericEntity<String> implements DataSourceConfigEntity {
    //数据源名称
    @Column
    private String name;

    //是否启用
    @Column
    private Long enabled;

    //创建日期
    @Column(name = "create_date")
    private java.util.Date createDate;

    //备注
    @Column
    private String describe;

    @Override
    @Column
    @ColumnType(jdbcType = JDBCType.LONGNVARCHAR)
    @JsonCodec
    public Map<String, Object> getProperties() {
        return super.getProperties();
    }

    @Override
    @Id
    @Column(name = "u_id")
    public String getId() {
        return super.getId();
    }
}