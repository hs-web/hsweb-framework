package org.hswebframework.web.starter.initialize;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.utils.ListUtils;

import java.io.Serializable;

@Slf4j
public class Version implements Comparable<Version>, Serializable {
    protected String name;
    protected String comment;
    protected String website;
    protected int majorVersion = 1;
    protected int minorVersion = 0;
    protected int revisionVersion = 0;
    protected boolean snapshot = false;

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
        version = version.toLowerCase();

        boolean snapshot = version.toLowerCase().contains("snapshot");

        String[] ver = version.split("[-]")[0].split("[.]");
        Integer[] numberVer = ListUtils.stringArr2intArr(ver);
        if (numberVer.length == 0) {
            numberVer = new Integer[]{1, 0, 0};
            log.warn("解析版本号失败:{},将使用默认版本号:1.0.0,请检查hsweb-starter.js配置内容!", version);
        }

        for (int i = 0; i < numberVer.length; i++) {
            if (numberVer[i] == null) {
                numberVer[i] = 0;
            }
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
