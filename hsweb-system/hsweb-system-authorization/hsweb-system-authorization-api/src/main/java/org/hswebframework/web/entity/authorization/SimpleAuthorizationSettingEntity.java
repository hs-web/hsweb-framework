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
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.List;

/**
 * 权限设置
 *
 * @author hsweb-generator-online
 */
@Getter
@Setter
@NoArgsConstructor
@Table(name = "s_autz_setting")
public class SimpleAuthorizationSettingEntity extends SimpleGenericEntity<String> implements AuthorizationSettingEntity {
    private static final long serialVersionUID = -6036823477895044483L;
    //类型
    @Column(length = 32)
    private String type;
    //设置给谁
    @Column(name = "setting_for", length = 32)
    private String settingFor;
    //状态
    @Column
    private Byte status;
    //备注
    @Column
    private String describe;

    private List<AuthorizationSettingMenuEntity> menus;

    private List<AuthorizationSettingDetailEntity> details;

    @Override
    @Id
    @Column(name = "u_id")
    public String getId() {
        return super.getId();
    }

}