package org.hsweb.web.starter;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author zhouhao
 */
@ConfigurationProperties(prefix = "hsweb.app")
public class AppProperties {
    private String name;
    private String comment;
    private String website;
    private String version;

    public SystemVersion build() {
        SystemVersion systemVersion = new SystemVersion();
        if (name == null) name = "default";
        if (version == null) {
            version = systemVersion.getFrameworkVersion().versionToString();
        }
        boolean snapshot = name.toLowerCase().contains("snapshot");
        name = name.toLowerCase().replace(".snapshot", "").replace("-snapshot", "");

        systemVersion.setName(name);
        systemVersion.setComment(comment);
        systemVersion.setWebsite(website);
        String[] strVer = version.split("[.]");
        systemVersion.setVersion(Integer.parseInt(strVer[0])
                , strVer.length > 1 ? Integer.parseInt(strVer[1]) : 0
                , strVer.length > 2 ? Integer.parseInt(strVer[2]) : 0
                , snapshot);
        return systemVersion;
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
