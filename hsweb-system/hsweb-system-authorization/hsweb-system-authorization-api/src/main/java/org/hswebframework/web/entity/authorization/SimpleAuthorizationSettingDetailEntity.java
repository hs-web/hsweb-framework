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
import org.hswebframework.ezorm.rdb.mapping.annotation.ColumnType;
import org.hswebframework.ezorm.rdb.mapping.annotation.JsonCodec;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

import javax.persistence.Column;
import javax.persistence.Table;
import java.sql.JDBCType;
import java.util.List;
import java.util.Set;

/**
 * 权限设置详情
 *
 * @author hsweb-generator-online
 */
@Getter
@Setter
@NoArgsConstructor
@Table(name = "s_autz_detail")
public class SimpleAuthorizationSettingDetailEntity extends SimpleGenericEntity<String> implements AuthorizationSettingDetailEntity {
    private static final long serialVersionUID = -4284551748747749521L;
    //权限id
    @Column(name = "permission_id",length = 32,nullable = false)
    private String                 permissionId;

    //设置id
    @Column(name = "setting_id",length = 32,nullable = false)
    private String                 settingId;
    //可操作类型

    @Column
    @ColumnType(jdbcType = JDBCType.LONGVARCHAR)
    @JsonCodec
    private Set<String>            actions;

    //数据权限控制
    @Column(name = "data_accesses")
    @ColumnType(jdbcType = JDBCType.LONGVARCHAR)
    @JsonCodec
    private List<DataAccessEntity> dataAccesses;

    //状态
    @Column
    private Byte                   status;

    //优先级
    @Column(precision = 32)
    private Long                   priority;


    //是否合并
    @Column
    private Boolean                merge;

}