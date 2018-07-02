package org.hswebframework.web.entity.form;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

import java.util.List;

/**
 * 动态表单
 *
 * @author hsweb-generator-online
 */
@Getter
@Setter
public class SimpleDynamicFormColumnEntity extends SimpleGenericEntity<String> implements DynamicFormColumnEntity {
    //表单ID
    private String       formId;
    //字段名称
    private String       name;
    //数据库列
    private String       columnName;
    //备注
    private String       describe;
    //别名
    private String       alias;
    //java类型
    private String       javaType;
    //jdbc类型
    private String       jdbcType;
    //数据类型
    private String       dataType;
    //长度
    private Integer      length;
    //精度
    private Integer      precision;
    //小数点位数
    private Integer      scale;
    //数据字典配置
    private String       dictConfig;
    //序号
    private Long         sortIndex;
    //验证器配置
    private List<String> validator;
}