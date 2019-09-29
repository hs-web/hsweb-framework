package org.hswebframework.web.entity.script;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.ezorm.rdb.mapping.annotation.ColumnType;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.JDBCType;

/**
 * 动态脚本
 *
 * @author hsweb-generator-online
 */
@Table(name = "s_script")
@Getter
@Setter
public class SimpleScriptEntity extends SimpleGenericEntity<String> implements ScriptEntity {
    //脚本名称
    @Column
    private String name;
    //类型
    @Column(length = 32)
    private String type;
    //脚本内容
    @Column
    @ColumnType(jdbcType = JDBCType.LONGVARCHAR)
    private String script;
    //脚本语言
    @Column(length = 16)
    private String language;
    //备注
    @Column
    private String remark;
    //状态
    @Column
    private Long status;
    //脚本标签
    @Column
    private String tag;

    @Override
    @Id
    @Column(name = "u_id")
    public String getId() {
        return super.getId();
    }

}