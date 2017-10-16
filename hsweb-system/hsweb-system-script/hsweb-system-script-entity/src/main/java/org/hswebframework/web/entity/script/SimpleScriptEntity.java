package org.hswebframework.web.entity.script;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

/**
* 动态脚本
* @author hsweb-generator-online
*/
public class SimpleScriptEntity extends SimpleGenericEntity<String> implements ScriptEntity{
  		//脚本名称
        private String name;
  		//类型
        private String type;
  		//脚本内容
        private String script;
  		//脚本语言
        private String language;
  		//备注
        private String remark;
  		//状态
        private Long status;
  		//脚本标签
        private String tag;

        /**
        * @return  脚本名称
        */
        @Override
        public String getName(){
			return this.name;
        }

        /**
        * @param  name  脚本名称
        */
        @Override
        public void setName(String name){
        	this.name=name;
        }
        /**
        * @return  类型
        */
        @Override
        public String getType(){
			return this.type;
        }

        /**
        * @param  type  类型
        */
        @Override
        public void setType(String type){
        	this.type=type;
        }
        /**
        * @return  脚本内容
        */
        @Override
        public String getScript(){
			return this.script;
        }

        /**
        * @param  script  脚本内容
        */
        @Override
        public void setScript(String script){
        	this.script=script;
        }
        /**
        * @return  脚本语言
        */
        @Override
        public String getLanguage(){
			return this.language;
        }

        /**
        * @param  language  脚本语言
        */
        @Override
        public void setLanguage(String language){
        	this.language=language;
        }
        /**
        * @return  备注
        */
        @Override
        public String getRemark(){
			return this.remark;
        }

        /**
        * @param  remark  备注
        */
        @Override
        public void setRemark(String remark){
        	this.remark=remark;
        }
        /**
        * @return  状态
        */
        @Override
        public Long getStatus(){
			return this.status;
        }

        /**
        * @param  status  状态
        */
        @Override
        public void setStatus(Long status){
        	this.status=status;
        }
        /**
        * @return  脚本标签
        */
        @Override
        public String getTag(){
			return this.tag;
        }

        /**
        * @param  tag  脚本标签
        */
        @Override
        public void setTag(String tag){
        	this.tag=tag;
        }
}