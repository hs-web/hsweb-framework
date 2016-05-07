package org.hsweb.web.crawler;


import java.util.Date;

/**
 * Created by zhouhao on 16-5-4.
 */
public class CrawlerResult {
    private String domain;

    private String url;

    private String content;

    private String html;

    private Date crawlerTime;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public Date getCrawlerTime() {
        return crawlerTime;
    }

    public void setCrawlerTime(Date crawlerTime) {
        this.crawlerTime = crawlerTime;
    }

    @Override
    public String toString() {
        return "CrawlerResult{" +
                "domain='" + domain + '\'' +
                ", url='" + url + '\'' +
                ", content='" + content + '\'' +
                ", html='" + html + '\'' +
                ", crawlerTime=" + crawlerTime +
                '}';
    }
}
