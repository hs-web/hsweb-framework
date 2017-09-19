package org.hswebframework.web.entity.form;

import org.hswebframework.web.commons.entity.SimpleGenericEntity;

/**
 * 动态表单
 *
 * @author hsweb-generator-online
 */
public class SimpleDynamicFormColumnEntity extends SimpleGenericEntity<String> implements DynamicFormColumnEntity {
    //表单ID
    private String  formId;
    //字段名称
    private String  name;
    //数据库列
    private String  columnName;
    //备注
    private String  describe;
    //别名
    private String  alias;
    //java类型
    private String  javaType;
    //jdbc类型
    private String  jdbcType;
    //数据类型
    private String  dataType;
    //长度
    private Integer length;
    //精度
    private Integer precision;
    //小数点位数
    private Integer scale;
    //字典ID
    private String  dictId;
    //字典解析器ID
    private String  dictParserId;

    //数据字典配置
    private String dictConfig;

    private Long sortIndex;

    /**
     * @return 表单ID
     */
    public String getFormId() {
        return this.formId;
    }

    /**
     * @param formId 表单ID
     */
    public void setFormId(String formId) {
        this.formId = formId;
    }

    /**
     * @return 字段名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param name 字段名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return 数据库列
     */
    public String getColumnName() {
        return this.columnName;
    }

    /**
     * @param columnName 数据库列
     */
    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    /**
     * @return 备注
     */
    public String getDescribe() {
        return this.describe;
    }

    /**
     * @param describe 备注
     */
    public void setDescribe(String describe) {
        this.describe = describe;
    }

    /**
     * @return 别名
     */
    public String getAlias() {
        return this.alias;
    }

    /**
     * @param alias 别名
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * @return java类型
     */
    public String getJavaType() {
        return this.javaType;
    }

    /**
     * @param javaType java类型
     */
    public void setJavaType(String javaType) {
        this.javaType = javaType;
    }

    /**
     * @return jdbc类型
     */
    public String getJdbcType() {
        return this.jdbcType;
    }

    /**
     * @param jdbcType jdbc类型
     */
    public void setJdbcType(String jdbcType) {
        this.jdbcType = jdbcType;
    }

    /**
     * @return 数据类型
     */
    public String getDataType() {
        return this.dataType;
    }

    /**
     * @param dataType 数据类型
     */
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    /**
     * @return 长度
     */
    public Integer getLength() {
        return this.length;
    }

    /**
     * @param length 长度
     */
    public void setLength(Integer length) {
        this.length = length;
    }

    /**
     * @return 精度
     */
    public Integer getPrecision() {
        return this.precision;
    }

    /**
     * @param precision 精度
     */
    public void setPrecision(Integer precision) {
        this.precision = precision;
    }

    /**
     * @return 小数点位数
     */
    public Integer getScale() {
        return this.scale;
    }

    /**
     * @param scale 小数点位数
     */
    public void setScale(Integer scale) {
        this.scale = scale;
    }

    public String getDictId() {
        return dictId;
    }

    public void setDictId(String dictId) {
        this.dictId = dictId;
    }

    public String getDictParserId() {
        return dictParserId;
    }

    public void setDictParserId(String dictParserId) {
        this.dictParserId = dictParserId;
    }

    @Override
    public String getDictConfig() {
        return dictConfig;
    }

    @Override
    public void setDictConfig(String dictConfig) {
        this.dictConfig = dictConfig;
    }


    @Override
    public Long getSortIndex() {
        return sortIndex;
    }

    @Override
    public void setSortIndex(Long sortIndex) {
        this.sortIndex = sortIndex;
    }
}