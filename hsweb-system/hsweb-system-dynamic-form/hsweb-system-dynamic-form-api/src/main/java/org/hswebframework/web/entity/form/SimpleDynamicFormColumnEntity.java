package org.hswebframework.web.entity.form;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.ezorm.rdb.mapping.annotation.ColumnType;
import org.hswebframework.ezorm.rdb.mapping.annotation.JsonCodec;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.JDBCType;
import java.util.List;

/**
 * 动态表单
 *
 * @author hsweb-generator-online
 */
@Getter
@Setter
@Table(name = "s_dyn_form_column")
public class SimpleDynamicFormColumnEntity extends SimpleGenericEntity<String> implements DynamicFormColumnEntity {
    //表单ID
    @Column(name = "form_id", length = 32)
    private String formId;
    //字段名称
    @Column
    private String name;
    //数据库列
    @Column(name = "column_name")
    private String columnName;
    //备注
    @Column
    private String describe;
    //别名
    @Column
    private String alias;
    //java类型
    @Column(name = "java_type")
    private String javaType;
    //jdbc类型
    @Column(name = "jdbc_type")
    private String jdbcType;
    //数据类型
    @Column(name = "data_type")
    private String dataType;
    //长度
    @Column
    private Integer length;
    //精度
    @Column
    private Integer precision;
    //小数点位数
    @Column
    private Integer scale;
    //数据字典配置
    @Column(name = "dict_config")
    private String dictConfig;
    //序号
    @Column(name = "sort_index")
    private Long sortIndex;
    //验证器配置
    @Column
    @ColumnType(jdbcType = JDBCType.CLOB)
    @JsonCodec
    private List<String> validator;

    @Override
    @Id
    @Column(name = "u_id")
    public String getId() {
        return super.getId();
    }
}