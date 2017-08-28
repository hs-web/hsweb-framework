package org.hswebframework.web.entity.datasource;
import org.hswebframework.web.commons.entity.GenericEntity;

/**
* 数据源配置 实体
* @author hsweb-generator-online
*/
public interface DataSourceConfigEntity extends GenericEntity<String>{
 /*-------------------------------------------
    |               属性名常量               |
    ===========================================*/
     /**
     * 数据源名称
     */
     String name="name";
     /**
     * 是否启用
     */
     String enabled="enabled";
     /**
     * 创建日期
     */
     String createDate="createDate";
     /**
     * 备注
     */
     String describe="describe";
    
        /**
        * @return 数据源名称
        */
        String getName();

        /**
        * @param  name  数据源名称
        */
        void setName(String name);
        /**
        * @return 是否启用
        */
        Long getEnabled();

        /**
        * @param  enabled  是否启用
        */
        void setEnabled(Long enabled);
        /**
        * @return 创建日期
        */
        java.util.Date getCreateDate();

        /**
        * @param  createDate  创建日期
        */
        void setCreateDate(java.util.Date createDate);
        /**
        * @return 备注
        */
        String getDescribe();

        /**
        * @param  describe  备注
        */
        void setDescribe(String describe);
      
}