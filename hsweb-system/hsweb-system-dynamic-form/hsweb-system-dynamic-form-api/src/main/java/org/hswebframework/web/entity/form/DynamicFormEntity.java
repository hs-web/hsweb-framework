package org.hswebframework.web.entity.form;

import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.NotBlank;
import org.hswebframework.web.commons.entity.GenericEntity;
import org.hswebframework.web.validator.group.CreateGroup;

/**
 * 动态表单 实体
 *
 * @author hsweb-generator-online
 */
public interface DynamicFormEntity extends GenericEntity<String> {
 /*-------------------------------------------
    |               属性名常量               |
    ===========================================*/
    /**
     * 表单名称
     */
    String name              = "name";
    /**
     * 数据库表名
     */
    String databaseTableName = "databaseTableName";
    /**
     * 备注
     */
    String describe          = "describe";
    /**
     * 版本
     */
    String version           = "version";
    /**
     * 创建人id
     */
    String creatorId         = "creatorId";
    /**
     * 创建时间
     */
    String createTime        = "createTime";
    /**
     * 修改时间
     */
    String updateTime        = "updateTime";
    /**
     * 是否已发布
     */
    String deployed          = "deployed";
    /**
     * 别名
     */
    String alias             = "alias";
    /**
     * 触发器
     */
    String triggers          = "triggers";
    /**
     * 表链接
     */
    String correlations      = "correlations";
    /**
     * 数据源id,为空使用默认数据源
     */
    String dataSourceId      = "dataSourceId";
    /**
     * 表单类型
     */
    String type              = "type";

    String tags = "tags";

    /**
     * @return 表单名称
     */
    @ApiModelProperty(value = "表单名称", required = true, example = "测试表单")
    @NotBlank(groups = CreateGroup.class)
    String getName();

    /**
     * @param name 表单名称
     */
    void setName(String name);

    /**
     * @return 数据库表名
     */
    @ApiModelProperty(value = "数据库表名", required = true, example = "f_test_form")
    @NotBlank(groups = CreateGroup.class)
    String getDatabaseTableName();

    /**
     * @param databaseTableName 数据库表名
     */
    void setDatabaseTableName(String databaseTableName);

    /**
     * @return 备注
     */

    String getDescribe();

    /**
     * @param describe 备注
     */
    void setDescribe(String describe);

    /**
     * @return 版本
     */
    @ApiModelProperty(value = "版本号,无需设置,每次保存自动自增.", example = "1")
    Long getVersion();

    /**
     * @param version 版本
     */
    void setVersion(Long version);

    /**
     * @return 创建人id
     */
    @ApiModelProperty(value = "创建人,根据当前用户自动获取.", example = "1")
    String getCreatorId();

    /**
     * @param creatorId 创建人id
     */
    void setCreatorId(String creatorId);

    /**
     * @return 创建时间
     */
    @ApiModelProperty(value = "创建时间,新增时自动设置.")
    Long getCreateTime();

    /**
     * @param createTime 创建时间
     */
    void setCreateTime(Long createTime);

    /**
     * @return 修改时间
     */
    @ApiModelProperty(value = "创建时间,修改时自动设置.")
    Long getUpdateTime();

    /**
     * @param updateTime 修改时间
     */
    void setUpdateTime(Long updateTime);

    /**
     * @return 是否已发布
     */
    @ApiModelProperty(value = "是否已发布,发布时自动设置.", example = "false")
    Boolean getDeployed();

    /**
     * @param deployed 是否已发布
     */
    void setDeployed(Boolean deployed);

    /**
     * @return 别名
     */
    @ApiModelProperty(value = "表别名.", example = "testForm")
    String getAlias();

    /**
     * @param alias 别名
     */
    void setAlias(String alias);

    /**
     * @return 触发器
     */
    @ApiModelProperty(value = "触发器.", example = "[{\"trigger\":\"update.before\",\"language\":\"groovy\",\"script\":\" return true;\"}]")
    String getTriggers();

    /**
     * @param triggers 触发器
     */
    void setTriggers(String triggers);

    /**
     * @return 表链接
     */
    String getCorrelations();

    /**
     * @param correlations 表链接
     */
    void setCorrelations(String correlations);

    /**
     * @return 数据源id, 为空使用默认数据源
     */
    String getDataSourceId();

    /**
     * @param dataSourceId 数据源id,为空使用默认数据源
     */
    void setDataSourceId(String dataSourceId);

    /**
     * @return 表单类型
     */
    String getType();

    /**
     * @param type 表单类型
     */
    void setType(String type);

    String getTags();

    void setTags(String tags);

}