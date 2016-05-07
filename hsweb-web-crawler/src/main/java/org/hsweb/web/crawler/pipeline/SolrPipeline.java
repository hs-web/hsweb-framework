package org.hsweb.web.crawler.pipeline;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.hsweb.web.crawler.CrawlerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by zhouhao on 16-5-4.
 */
public class SolrPipeline extends AbstractPipeline {
    private SolrServer solrServer;
    private String core = "hsweb-crawler";
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public SolrPipeline(String core) {
        this.solrServer = new HttpSolrServer("http://127.0.0.1:8983/solr/" + core);
    }

    public SolrPipeline(SolrServer solrServer) {
        this.solrServer = solrServer;
    }

    @Override
    public void process(CrawlerResult result) {
        try {
            logger.debug("save CrawlerResult " + result.getUrl());
            SolrInputDocument document = new SolrInputDocument();
            document.addField("url", result.getUrl());
//            document.addField("html", result.getHtml());
            document.addField("content_text_cn", result.getContent());
            document.addField("domain", result.getDomain());
            solrServer.add(document, 1000);
        } catch (Exception e) {
            logger.error("save CrawlerResult error!", e);
        }

    }
}
