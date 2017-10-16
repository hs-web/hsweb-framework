package org.hswebframework.web.entity.template;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

/**
* 模板
* @author hsweb-generator-online
*/
public class SimpleTemplateEntity extends SimpleGenericEntity<String> implements TemplateEntity {
  		//模板名称
        private String name;
  		//模板类型
        private String type;
  		//模板内容
        private String template;
  		//模板配置
        private String config;
  		//版本号
        private Long version;
  		//模板分类
        private String classified;

        /**
        * @return  模板名称
        */
        @Override
        public String getName(){
			return this.name;
        }

        /**
        * @param  name  模板名称
        */
        @Override
        public void setName(String name){
        	this.name=name;
        }
        /**
        * @return  模板类型
        */
        @Override
        public String getType(){
			return this.type;
        }

        /**
        * @param  type  模板类型
        */
        @Override
        public void setType(String type){
        	this.type=type;
        }
        /**
        * @return  模板内容
        */
        @Override
        public String getTemplate(){
			return this.template;
        }

        /**
        * @param  template  模板内容
        */
        @Override
        public void setTemplate(String template){
        	this.template=template;
        }
        /**
        * @return  模板配置
        */
        @Override
        public String getConfig(){
			return this.config;
        }

        /**
        * @param  config  模板配置
        */
        @Override
        public void setConfig(String config){
        	this.config=config;
        }
        /**
        * @return  版本号
        */
        @Override
        public Long getVersion(){
			return this.version;
        }

        /**
        * @param  version  版本号
        */
        @Override
        public void setVersion(Long version){
        	this.version=version;
        }
        /**
        * @return  模板分类
        */
        @Override
        public String getClassified(){
			return this.classified;
        }

        /**
        * @param  classified  模板分类
        */
        @Override
        public void setClassified(String classified){
        	this.classified=classified;
        }
}