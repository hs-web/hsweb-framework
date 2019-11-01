package org.hswebframework.web.starter.initialize;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

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
