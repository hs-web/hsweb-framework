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

import org.hswebframework.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SystemVersion extends Version  {

    public SystemVersion() {
    }

    public SystemVersion(String version) {
        this.setVersion(version);
    }

    private FrameworkVersion frameworkVersion = new FrameworkVersion();

    private List<Dependency> dependencies = new ArrayList<>();

    public FrameworkVersion getFrameworkVersion() {
        return frameworkVersion;
    }

    public void setFrameworkVersion(FrameworkVersion frameworkVersion) {
        this.frameworkVersion = frameworkVersion;
    }

    public List<Dependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<Dependency> dependencies) {
        this.dependencies = dependencies;
        initDepCache();
    }

    private Map<String, Dependency> depCache;

    protected String getDepKey(String groupId, String artifactId) {
        return StringUtils.concat(groupId, "/", artifactId);
    }

    protected void initDepCache() {
        depCache = new HashMap<>();
        dependencies.forEach(dependency -> depCache.put(getDepKey(dependency.groupId, dependency.artifactId), dependency));
    }

    public Dependency getDependency(String groupId, String artifactId) {
        if (depCache == null) {
            initDepCache();
        }
        return depCache.get(getDepKey(groupId, artifactId));
    }

    public static class FrameworkVersion extends Version {
        public FrameworkVersion() {
            setName("hsweb framework");
            setComment("企业后台管理系统基础框架");
            setWebsite("http://www.hsweb.me");
            setComment("");
            setVersion(3, 1, 0, true);
        }
    }


    public interface Property {
        /**
         * @see SystemVersion#name
         */
        String name            = "name";
        /**
         * @see SystemVersion#comment
         */
        String comment         = "comment";
        /**
         * @see SystemVersion#website
         */
        String website         = "website";
        /**
         * @see SystemVersion#majorVersion
         */
        String majorVersion    = "majorVersion";
        /**
         * @see SystemVersion#minorVersion
         */
        String minorVersion    = "minorVersion";
        /**
         * @see SystemVersion#revisionVersion
         */
        String revisionVersion = "revisionVersion";
        /**
         * @see SystemVersion#snapshot
         */
        String snapshot        = "snapshot";

        /**
         * @see SystemVersion#frameworkVersion
         */
        String frameworkVersion = "frameworkVersion";

        /**
         * @see SystemVersion#dependencies
         */
        String dependencies = "dependencies";
    }


}
