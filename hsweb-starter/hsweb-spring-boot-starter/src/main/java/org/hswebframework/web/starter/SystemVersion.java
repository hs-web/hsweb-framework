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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.utils.ListUtils;
import org.hswebframework.utils.StringUtils;

import java.util.*;

public class SystemVersion extends Version {

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
            setVersion(3, 0, 0, true);
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

    public static class Dependency extends Version {
        protected String groupId;
        protected String artifactId;
        protected String author;

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public void setArtifactId(String artifactId) {
            this.artifactId = artifactId;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public static Dependency fromMap(Map<String, Object> map) {
            Dependency dependency = new Dependency();
            dependency.setGroupId((String) map.get("groupId"));
            dependency.setArtifactId((String) map.get("artifactId"));
            dependency.setName((String) map.getOrDefault(Property.name, dependency.getArtifactId()));
            dependency.setVersion((String) map.get("version"));
            dependency.setWebsite((String) map.get(Property.website));
            dependency.setAuthor((String) map.get("author"));
            return dependency;
        }

        public boolean isSameDependency(Dependency dependency) {
            return isSameDependency(dependency.getGroupId(), dependency.getArtifactId());
        }

        public boolean isSameDependency(String groupId, String artifactId) {
            return groupId.equals(this.getGroupId()) && artifactId.equals(this.getArtifactId());
        }

        @Override
        public String toString() {
            return JSON.toJSONString(this, SerializerFeature.PrettyFormat);
        }
    }
}

@Slf4j
class Version implements Comparable<Version> {
    protected String  name;
    protected String  comment;
    protected String  website;
    protected int     majorVersion    = 1;
    protected int     minorVersion    = 0;
    protected int     revisionVersion = 0;
    protected boolean snapshot        = false;

    public void setVersion(int major, int minor, int revision, boolean snapshot) {
        this.majorVersion = major;
        this.minorVersion = minor;
        this.revisionVersion = revision;
        this.snapshot = snapshot;
    }

    public void setVersion(String version) {
        if (null == version) {
            return;
        }
        boolean snapshot = version.toLowerCase().contains("snapshot");
        version = version.toLowerCase()
                .replace(".snapshot", "")
                .replace("-snapshot", "")
                .replace("-rc", "")
                .replace("-release", "");
        String[] ver = version.split("[.]");
        Integer[] numberVer = ListUtils.stringArr2intArr(ver);
        if (numberVer.length < 1 || Arrays.stream(numberVer).anyMatch(Objects::isNull)) {
            numberVer = new Integer[]{1, 0, 0};
            log.warn("解析版本号失败:{},将使用默认版本号:1.0.0,请检查hsweb-starter.js配置内容!", version);
        }
        setVersion(numberVer[0],
                numberVer.length <= 1 ? 0 : numberVer[1],
                numberVer.length <= 2 ? 0 : numberVer[2],
                snapshot);
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getWebsite() {
        if (website == null) {
            website = "";
        }
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public void setMajorVersion(int majorVersion) {
        this.majorVersion = majorVersion;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public void setMinorVersion(int minorVersion) {
        this.minorVersion = minorVersion;
    }

    public int getRevisionVersion() {
        return revisionVersion;
    }

    public void setRevisionVersion(int revisionVersion) {
        this.revisionVersion = revisionVersion;
    }

    public boolean isSnapshot() {
        return snapshot;
    }

    public void setSnapshot(boolean snapshot) {
        this.snapshot = snapshot;
    }

    @Override
    public int compareTo(Version o) {
        if (null == o) {
            return -1;
        }
        if (o.getMajorVersion() > this.getMajorVersion()) {
            return -1;
        }
        if (o.getMajorVersion() == this.getMajorVersion()) {
            if (o.getMinorVersion() > this.getMinorVersion()) {
                return -1;
            }
            if (o.getMinorVersion() == this.getMinorVersion()) {
                return Integer.compare(this.getRevisionVersion(), o.getRevisionVersion());
            } else {
                return 1;
            }
        } else {
            return 1;
        }
    }

    public String versionToString() {
        return String.valueOf(majorVersion) + "." +
                minorVersion + "." +
                revisionVersion + (snapshot ? "-SNAPSHOT" : "");
    }

    @Override
    public String toString() {
        return name + " version " +
                majorVersion + "." +
                minorVersion + "." +
                revisionVersion + (snapshot ? "-SNAPSHOT" : "");
    }

}
