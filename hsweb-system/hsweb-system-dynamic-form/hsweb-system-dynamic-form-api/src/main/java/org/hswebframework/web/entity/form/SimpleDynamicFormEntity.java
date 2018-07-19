package org.hswebframework.web.entity.form;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

/**
 * 动态表单
 *
 * @author hsweb-generator-online
 */
@Getter
@Setter
public class SimpleDynamicFormEntity extends SimpleGenericEntity<String> implements DynamicFormEntity {
    //表单名称
    private String  name;
    //数据库表名
    private String  databaseTableName;
    //备注
    private String  describe;
    //版本
    private Long    version;
    //创建人id
    private String  creatorId;
    //创建时间
    private Long    createTime;
    //修改时间
    private Long    updateTime;
    //是否已发布
    private Boolean deployed;
    //别名
    private String  alias;
    //触发器
    private String  triggers;
    //表链接
    private String  correlations;
    //数据源id,为空使用默认数据源
    private String  dataSourceId;
    //表单类型
    private String  type;

    private String tags;


}