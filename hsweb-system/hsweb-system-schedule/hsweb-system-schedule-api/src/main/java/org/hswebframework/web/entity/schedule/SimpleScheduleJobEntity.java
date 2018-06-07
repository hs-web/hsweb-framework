package org.hswebframework.web.entity.schedule;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

/**
* 调度任务
* @author hsweb-generator-online
*/
public class SimpleScheduleJobEntity extends SimpleGenericEntity<String> implements ScheduleJobEntity{
  		//任务名称
        private String name;
  		//备注
        private String remark;
  		//定时调度配置
        private String quartzConfig;
  		//执行脚本
        private String script;
  		//脚本语言
        private String language;
  		//是否启用
        private Byte   status;
  		//启动参数
        private String parameters;
  		//任务类型
        private String type;
  		//标签
        private String tags;

        /**
        * @return  任务名称
        */
        @Override
        public String getName(){
			return this.name;
        }

        /**
        * @param  name  任务名称
        */
        @Override
        public void setName(String name){
        	this.name=name;
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
        * @return  定时调度配置
        */
        @Override
        public String getQuartzConfig(){
			return this.quartzConfig;
        }

        /**
        * @param  quartzConfig  定时调度配置
        */
        @Override
        public void setQuartzConfig(String quartzConfig){
        	this.quartzConfig = quartzConfig;
        }
        /**
        * @return  执行脚本
        */
        @Override
        public String getScript(){
			return this.script;
        }

        /**
        * @param  script  执行脚本
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
        * @return  是否启用
        */
        @Override
        public Byte getStatus(){
			return this.status;
        }

        /**
        * @param  status  是否启用
        */
        @Override
        public void setStatus(Byte status){
        	this.status = status;
        }
        /**
        * @return  启动参数
        */
        @Override
        public String getParameters(){
			return this.parameters;
        }

        /**
        * @param  parameters  启动参数
        */
        @Override
        public void setParameters(String parameters){
        	this.parameters=parameters;
        }
        /**
        * @return  任务类型
        */
        @Override
        public String getType(){
			return this.type;
        }

        /**
        * @param  type  任务类型
        */
        @Override
        public void setType(String type){
        	this.type=type;
        }
        /**
        * @return  标签
        */
        @Override
        public String getTags(){
			return this.tags;
        }

        /**
        * @param  tags  标签
        */
        @Override
        public void setTags(String tags){
        	this.tags=tags;
        }
}