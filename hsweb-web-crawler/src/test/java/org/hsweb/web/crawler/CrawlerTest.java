package org.hsweb.web.crawler;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.hsweb.web.crawler.pipeline.SolrPipeline;
import org.hsweb.web.crawler.processor.SimplePageProcessor;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.scheduler.QueueScheduler;

/**
 * Created by zhouhao on 16-5-4.
 */
public class CrawlerTest {

    public static void main(String[] args) throws SolrServerException {
//        Spider.create(new SimplePageProcessor("http://www.yiliu88.com*", "http://www.yiliu88.com/*.html"))
//                .addUrl("http://www.yiliu88.com")
//                .setScheduler(new QueueScheduler())
//                .addPipeline(new SolrPipeline("test"))
//                .thread(5)
//                .run();
//        HttpSolrServer server = new HttpSolrServer("http://127.0.0.1:8983/solr/test");
//        SolrQuery query = new SolrQuery();
//        query.setQuery("content_txt_en:hello zhangsan");
//        //mlt在查询时，打开/关闭 MoreLikeThisComponent 的布尔值
//        query.setParam("mlt", "true");
//        //fl 需要返回的字段
//        query.setParam("fl", "content_txt_en,id");
//        //mtl.fl 根据哪些字段判断相似度
//        query.setParam("mlt.fl", "content_txt_en");
//        //mlt.mintf 最小分词频率，在单个文档中出现频率小于这个值的词将不用于相似判断
//        query.setParam("mlt.mintf", "1");
//        //mlt.mindf 最小文档频率，所在文档的个数小于这个值的词将不用于相似判断
//        query.setParam("mlt.mindf", "1");
//        query.setParam("hl", "true");
//        query.setParam("hl.fl","content_txt_en");
//        query.setParam("hl.simple.pre","<span style='hl'>");
//        query.setParam("hl.simple.post","</span>");
//
//        QueryResponse response = server.query(query);
//        response.getResponse().forEach(stringObjectEntry -> {
//            System.out.println(stringObjectEntry);
//        });

    }
}
