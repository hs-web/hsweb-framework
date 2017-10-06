package org.hswebframework.web.entity.form;

import org.hibernate.validator.constraints.NotBlank;
import org.hswebframework.web.commons.entity.GenericEntity;
import org.hswebframework.web.validator.group.CreateGroup;

/**
 * 动态表单 实体
 *
 * @author hsweb-generator-online
 */
public interface DynamicFormColumnEntity extends GenericEntity<String> {
 /*-------------------------------------------
    |               属性名常量               |
    ===========================================*/
    /**
     * 表单ID
     */
    String formId     = "formId";
    /**
     * 字段名称
     */
    String name       = "name";
    /**
     * 数据库列
     */
    String columnName = "columnName";
    /**
     * 备注
     */
    String describe   = "describe";
    /**
     * 别名
     */
    String alias      = "alias";
    /**
     * java类型
     */
    String javaType   = "javaType";
    /**
     * jdbc类型
     */
    String jdbcType   = "jdbcType";
    /**
     * 数据类型
     */
    String dataType   = "dataType";
    /**
     * 长度
     */
    String length     = "length";
    /**
     * 精度
     */
    String precision  = "precision";
    /**
     * 小数点位数
     */
    String scale      = "scale";

    /**
     * 数据字典配置
     */
    String dictConfig = "dictConfig";

    /**
     * 字典ID
     */
    String dictId = "dictId";

    /**
     * 字典解析器ID
     */
    String dictParserId = "dictParserId";

    /**
     * 其他配置
     */
    String properties = "properties";

    /**
     * 排序序号
     */
    String sortIndex = "sortIndex";

    /**
     * @return 表单ID
     */
    @NotBlank(groups = CreateGroup.class)
    String getFormId();

    /**
     * @param formId 表单ID
     */
    void setFormId(String formId);

    /**
     * @return 字段名称
     */
    @NotBlank(groups = CreateGroup.class)
    String getName();

    /**
     * @param name 字段名称
     */
    void setName(String name);

    /**
     * @return 数据库列
     */
    @NotBlank(groups = CreateGroup.class)
    String getColumnName();

    /**
     * @param columnName 数据库列
     */
    void setColumnName(String columnName);

    /**
     * @return 备注
     */
    String getDescribe();

    /**
     * @param describe 备注
     */
    void setDescribe(String describe);

    /**
     * @return 别名
     */
    String getAlias();

    /**
     * @param alias 别名
     */
    void setAlias(String alias);

    /**
     * @return java类型
     */
    @NotBlank(groups = CreateGroup.class)
    String getJavaType();

    /**
     * @param javaType java类型
     */
    void setJavaType(String javaType);

    /**
     * @return jdbc类型
     */
    @NotBlank(groups = CreateGroup.class)
    String getJdbcType();

    /**
     * @param jdbcType jdbc类型
     */
    void setJdbcType(String jdbcType);

    /**
     * @return 数据类型
     */
    String getDataType();

    /**
     * @param dataType 数据类型
     */
    void setDataType(String dataType);

    /**
     * @return 长度
     */
    Integer getLength();

    /**
     * @param length 长度
     */
    void setLength(Integer length);

    /**
     * @return 精度
     */
    Integer getPrecision();

    /**
     * @param precision 精度
     */
    void setPrecision(Integer precision);

    /**
     * @return 小数点位数
     */
    Integer getScale();

    /**
     * @param scale 小数点位数
     */
    void setScale(Integer scale);

    @Deprecated
    String getDictId();

    @Deprecated
    void setDictId(String dictId);

    @Deprecated
    String getDictParserId();

    @Deprecated
    void setDictParserId(String dictParserId);

    void setDictConfig(String dictConfig);

    String getDictConfig();

    Long getSortIndex();

    void setSortIndex(Long sortIndex);
}