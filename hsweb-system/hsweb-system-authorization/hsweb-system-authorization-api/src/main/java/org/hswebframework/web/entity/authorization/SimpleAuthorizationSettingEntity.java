/*
 *  Copyright 2016 http://www.hswebframework.org
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package org.hswebframework.web.entity.authorization;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

import java.util.List;

/**
 * 权限设置
 *
 * @author hsweb-generator-online
 */
@Getter
@Setter
@NoArgsConstructor
public class SimpleAuthorizationSettingEntity extends SimpleGenericEntity<String> implements AuthorizationSettingEntity {
    private static final long serialVersionUID = -6036823477895044483L;
    //类型
    private String type;
    //设置给谁
    private String settingFor;
    //状态
    private Byte   status;
    //备注
    private String describe;

    private List<AuthorizationSettingMenuEntity> menus;

    private List<AuthorizationSettingDetailEntity> details;


}