/*
 *
 *  * Copyright 2016 http://www.hswebframework.org
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.hswebframework.web.commons.entity;


import org.hibernate.validator.constraints.NotBlank;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public abstract class SimpleSharedEntity<PK> extends SimpleGenericEntity<PK> implements SharedEntity {
    @NotBlank
    private String creatorId;

    @NotBlank
    private String shareCode;

    @Override
    public String getCreatorId() {
        return creatorId;
    }

    @Override
    public SimpleSharedEntity<PK> setCreatorId(String creatorId) {
        this.creatorId = creatorId;
        return this;
    }

    @Override
    public String getShareCode() {
        return shareCode;
    }

    @Override
    public SimpleSharedEntity<PK> setShareCode(String shareCode) {
        this.shareCode = shareCode;
        return this;
    }
}
