/*
 * Copyright 2015-2016 http://hsweb.me
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hsweb.web.starter;

public class SystemVersion extends Version {

    private FrameworkVersion frameworkVersion = new FrameworkVersion();

    public FrameworkVersion getFrameworkVersion() {
        return frameworkVersion;
    }

    public void setFrameworkVersion(FrameworkVersion frameworkVersion) {
        this.frameworkVersion = frameworkVersion;
    }

    public static class FrameworkVersion extends Version {
        public FrameworkVersion() {
            setName("hsweb framework");
            setComment("企业后台管理系统基础框架");
            setWebsite("http://www.hsweb.me");
            setComment("");
            setVersion(2, 3, 0, true);
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

        String frameworkVersion = "frameworkVersion";
    }
}

class Version implements Comparable<Version> {
    protected String name;
    protected String comment;
    protected String website;
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
        if (null == o) return -1;
        if (o.getMajorVersion() > this.getMajorVersion()) return -1;
        if (o.getMajorVersion() == this.getMajorVersion()) {
            if (o.getMinorVersion() > this.getMinorVersion()) return -1;
            if (o.getMinorVersion() == this.getMinorVersion()) {
                if (o.getRevisionVersion() > this.getRevisionVersion()) return -1;
                if (o.getRevisionVersion() == this.getRevisionVersion()) return 0;
                return 1;
            } else {
                return 1;
            }
        } else {
            return 1;
        }
    }

    public String versionToString() {
        return new StringBuilder()
                .append(majorVersion).append(".")
                .append(minorVersion).append(".")
                .append(revisionVersion).append(snapshot ? ".SNAPSHOT" : "").toString();
    }

    @Override
    public String toString() {
        return new StringBuilder(name).append(" version ")
                .append(majorVersion).append(".")
                .append(minorVersion).append(".")
                .append(revisionVersion).append(snapshot ? ".SNAPSHOT" : "").toString();
    }
}
