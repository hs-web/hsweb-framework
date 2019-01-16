/*
 *
 *  * Copyright 2019 http://www.hswebframework.org
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

package org.hswebframework.web.starter;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author zhouhao
 */
@ConfigurationProperties(prefix = "hsweb.app")
@Getter
@Setter
public class AppProperties {
    private boolean      autoInit = true;
    private List<String> initTableExcludes;

    private String name;
    private String comment;
    private String website;
    private String version;

    public SystemVersion build() {
        SystemVersion systemVersion = new SystemVersion();
        systemVersion.setName(name);
        systemVersion.setComment(comment);
        systemVersion.setWebsite(website);
        systemVersion.setVersion(version);
        return systemVersion;
    }
}
