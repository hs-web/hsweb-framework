/*
 * Copyright 2015-2016 http://hsweb.me
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hsweb.web.oauth2.po;

import org.hsweb.web.bean.po.GenericPo;

/**
* OAuth2客户端
* Created by hsweb-generator Aug 16, 2016 7:39:55 AM
*/
public class OAuth2Client extends GenericPo<String> {
  		//关联用户
        private String userId;
  		//客户端名称
        private String name;
  		//密钥
        private String secret;
  		//备注
        private String comment;
  		//状态
        private int status;

        /**
        * 获取 关联用户
        * @return String 关联用户
        */
        public String getUserId(){
			return this.userId;
        }

        /**
        * 设置 关联用户
        * @param userId 关联用户
        */
        public void setUserId(String userId){
        	this.userId=userId;
        }
        /**
        * 获取 客户端名称
        * @return String 客户端名称
        */
        public String getName(){
			return this.name;
        }

        /**
        * 设置 客户端名称
        * @param name 客户端名称
        */
        public void setName(String name){
        	this.name=name;
        }
        /**
        * 获取 密钥
        * @return String 密钥
        */
        public String getSecret(){
			return this.secret;
        }

        /**
        * 设置 密钥
        * @param secret 密钥
        */
        public void setSecret(String secret){
        	this.secret=secret;
        }
        /**
        * 获取 备注
        * @return String 备注
        */
        public String getComment(){
			return this.comment;
        }

        /**
        * 设置 备注
        * @param comment 备注
        */
        public void setComment(String comment){
        	this.comment=comment;
        }
        /**
        * 获取 状态
        * @return int 状态
        */
        public int getStatus(){
			return this.status;
        }

        /**
        * 设置 状态
        * @param status 状态
        */
        public void setStatus(int status){
        	this.status=status;
        }
}