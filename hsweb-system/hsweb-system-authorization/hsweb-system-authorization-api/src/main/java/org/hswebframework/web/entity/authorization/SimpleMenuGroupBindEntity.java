/*
 *  Copyright 2019 http://www.hswebframework.org
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
import org.hswebframework.web.commons.entity.SimpleTreeSortSupportEntity;

import java.util.List;

/**
 * 菜单分组关联
 *
 * @author hsweb-generator-online
 */
@Getter
@Setter
@NoArgsConstructor
public class SimpleMenuGroupBindEntity extends SimpleTreeSortSupportEntity<String> implements MenuGroupBindEntity {
    private static final long serialVersionUID = -8671671135008425741L;
    //状态
    private Byte                      status;
    //菜单id
    private String                    menuId;
    //分组id
    private String                    groupId;
    //子节点
    private List<MenuGroupBindEntity> children;
}