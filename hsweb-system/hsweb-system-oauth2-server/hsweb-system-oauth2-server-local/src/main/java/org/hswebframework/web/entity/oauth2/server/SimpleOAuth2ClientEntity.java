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
 *
 */

package org.hswebframework.web.entity.oauth2.server;

import lombok.*;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

import java.util.Set;

/**
 * @author zhouhao
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimpleOAuth2ClientEntity extends SimpleGenericEntity<String> implements OAuth2ClientEntity {
    private static final long serialVersionUID = -8370400980996896599L;
    private String name;

    private String secret;

    private String redirectUri;

    private String ownerId;

    private String creatorId;

    private Long createTime;

    private String type;

    private String describe;

    private Set<String> supportGrantTypes;

    private Set<String> defaultGrantScope;

    private Byte status;

}
