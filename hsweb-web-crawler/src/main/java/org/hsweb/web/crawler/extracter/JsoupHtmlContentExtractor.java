package org.hsweb.web.crawler.extracter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Created by 浩 on 2015-09-07 0007.
 */
public class JsoupHtmlContentExtractor implements HtmlContentExtractor {

    public JsoupHtmlContentExtractor(String select) {
        this.select = select;
    }

    private String select;

    @Override
    public String parse(String html) {
        Document document = Jsoup.parse(html);
        if (select == null)
            return document.text();
        return document.select(select).text();
    }

    public String getSelect() {
        return select;
    }

    public void setSelect(String select) {
        this.select = select;
    }
}
