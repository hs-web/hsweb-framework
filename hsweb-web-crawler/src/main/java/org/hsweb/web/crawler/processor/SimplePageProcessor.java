package org.hsweb.web.crawler.processor;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.utils.UrlUtils;

import java.util.List;

/**
 * Created by zhouhao on 16-5-4.
 */
public class SimplePageProcessor implements PageProcessor {
    private Site site;
    /**
     * 要抓取的页面
     */
    private String crawlerUrlPattern;

    /**
     * 要保存的页面
     */
    private String saveUrlPattern;


    public SimplePageProcessor(String crawlerUrlPattern, String saveUrlPattern) {
        if (site == null)
            this.site = Site.me().setSleepTime(1000).setRetryTimes(5).setUseGzip(true);
        this.crawlerUrlPattern = "(" + crawlerUrlPattern.replace(".", "\\.").replace("*", "[^\"'#]*") + ")";
        this.saveUrlPattern = "(" + saveUrlPattern.replace(".", "\\.").replace("*", "[^\"'#]*") + ")";
    }

    @Override
    public void process(Page page) {
        List<String> requests = page.getHtml().links().regex(crawlerUrlPattern).all();
        page.addTargetRequests(requests);
        if (!page.getUrl().regex(saveUrlPattern).match())
            page.setSkip(true);
        page.putField("page", page);
    }

    @Override
    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }
}
