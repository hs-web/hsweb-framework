package org.hswebframework.web.starter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.Map;

public  class Dependency extends Version {
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
            dependency.setName((String) map.getOrDefault(SystemVersion.Property.name, dependency.getArtifactId()));
            dependency.setVersion((String) map.get("version"));
            dependency.setWebsite((String) map.get(SystemVersion.Property.website));
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