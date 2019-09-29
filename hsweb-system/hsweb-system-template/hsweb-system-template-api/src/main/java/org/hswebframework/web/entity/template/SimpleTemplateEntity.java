package org.hswebframework.web.entity.template;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.ezorm.rdb.mapping.annotation.ColumnType;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

import javax.persistence.Column;
import javax.persistence.Table;
import java.sql.JDBCType;

/**
 * 模板
 *
 * @author hsweb-generator-online
 */
@Table(name = "s_template")
@Getter
@Setter
public class SimpleTemplateEntity extends SimpleGenericEntity<String> implements TemplateEntity {
    //模板名称
    @Column
    private String name;

    //模板类型
    @Column
    private String type;

    //模板内容
    @Column
    @ColumnType(jdbcType = JDBCType.LONGVARCHAR)
    private String template;

    //模板配置
    @Column
    @ColumnType(jdbcType = JDBCType.LONGVARCHAR)
    private String config;

    //版本号
    @Column
    private Long version;

    //模板分类
    @Column
    private String classified;


}