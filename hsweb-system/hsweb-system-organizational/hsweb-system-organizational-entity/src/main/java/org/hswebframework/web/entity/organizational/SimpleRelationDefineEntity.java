package org.hswebframework.web.entity.organizational;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

/**
* 关系定义
* @author hsweb-generator-online
*/
public class SimpleRelationDefineEntity extends SimpleGenericEntity<String> implements RelationDefineEntity{
  		//关系名称
        private String name;
  		//关系类型ID
        private String typeId;
  		//状态
        private Byte status;

        /**
        * @return  关系名称
        */
        @Override
        public String getName(){
			return this.name;
        }

        /**
        * @param  name  关系名称
        */
        @Override
        public void setName(String name){
        	this.name=name;
        }
        /**
        * @return  关系类型ID
        */
        @Override
        public String getTypeId(){
			return this.typeId;
        }

        /**
        * @param  typeId  关系类型ID
        */
        @Override
        public void setTypeId(String typeId){
        	this.typeId=typeId;
        }
        /**
        * @return  状态
        */
        @Override
        public Byte getStatus(){
			return this.status;
        }

        /**
        * @param  status  状态
        */
        @Override
        public void setStatus(Byte status){
        	this.status=status;
        }
}