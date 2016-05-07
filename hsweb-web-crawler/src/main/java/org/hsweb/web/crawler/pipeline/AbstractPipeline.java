package org.hsweb.web.crawler.pipeline;

import org.hsweb.web.crawler.CrawlerResult;
import org.hsweb.web.crawler.extracter.DefaultHtmlContentExtractor;
import org.hsweb.web.crawler.extracter.HtmlContentExtractor;
import org.hsweb.web.crawler.extracter.JsoupHtmlContentExtractor;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.utils.UrlUtils;

import java.util.Date;

/**
 * Created by zhouhao on 16-5-4.
 */
public abstract class AbstractPipeline implements Pipeline {


    public HtmlContentExtractor extractor;

    public AbstractPipeline() {
        this(new DefaultHtmlContentExtractor(35));
    }

    public AbstractPipeline(String selector) {
        this(new JsoupHtmlContentExtractor(selector));
    }

    public AbstractPipeline(HtmlContentExtractor extractor) {
        this.extractor = extractor;
    }

    @Override
    public void process(ResultItems resultItems, Task task) {
        if (resultItems.isSkip()) return;
        Page page = resultItems.get("page");
        CrawlerResult result = new CrawlerResult();
        result.setCrawlerTime(new Date());
        result.setUrl(page.getUrl().get());
        result.setDomain(UrlUtils.getDomain(result.getUrl()));
        result.setHtml(page.getHtml().get());
        result.setContent(extractor.parse(result.getHtml()));
        process(result);
    }

    public abstract void process(CrawlerResult result);
}
