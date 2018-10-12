package org.hswebframework.web.tests

import com.alibaba.fastjson.JSON
import org.hswebframework.ezorm.core.dsl.Query
import org.hswebframework.web.WebUtil
import org.hswebframework.web.commons.entity.param.QueryParamEntity
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*


/**
 * @author zhouhao
 * @since 3.0.2
 */
abstract class HswebCrudWebApiSpecification extends HswebSpecification {

    protected abstract String getBaseApi();

    def doAddRequest(String requestBody) {
        def response = mockMvc.perform(post(getBaseApi())
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .response
                .contentAsString;
        return JSON.parseObject(response);
    }

    def doUpdateRequest(String id, String requestBody) {
        def response = mockMvc.perform(put("${getBaseApi()}/{id}", id)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .response
                .contentAsString;
        return JSON.parseObject(response);
    }

    def doDeleteRequest(String id) {
        def response = mockMvc
                .perform(delete("${getBaseApi()}/{id}", id))
                .andReturn()
                .response
                .contentAsString;
        return JSON.parseObject(response);
    }

    def doGetRequest(String id) {
        def response = mockMvc
                .perform(get("${getBaseApi()}/{id}", id))
                .andReturn()
                .response
                .contentAsString;
        return JSON.parseObject(response);
    }


    Query createQuery() {
        return Query.empty(new QueryParamEntity());
    }

    def doQueryRequest(Query query) {
        MockHttpServletRequestBuilder get = get("${getBaseApi()}")
        WebUtil.objectToHttpParameters(query.param)
                .forEach({ k, v -> get.param(k, v) })
        def response = mockMvc
                .perform(get)
                .andReturn()
                .response
                .contentAsString;
        return JSON.parseObject(response);
    }

    def doQueryByIdsRequest(String ids) {
        def response = mockMvc
                .perform(get("${getBaseApi()}/ids").param("ids", ids))
                .andReturn()
                .response
                .contentAsString;
        return JSON.parseObject(response);
    }

    def doTotalRequest(Query query) {
        MockHttpServletRequestBuilder get = get("${getBaseApi()}/total")
        WebUtil.objectToHttpParameters(query.param)
                .forEach({ k, v -> get.param(k, v) })

        def response = mockMvc
                .perform(get)
                .andReturn()
                .response
                .contentAsString;
        return JSON.parseObject(response);
    }

    def doCountRequest(Query query) {
        MockHttpServletRequestBuilder get = get("${getBaseApi()}/count")
        WebUtil.objectToHttpParameters(query.param)
                .forEach({ k, v -> get.param(k, v) })
        def response = mockMvc
                .perform(get)
                .andReturn()
                .response
                .contentAsString;
        return JSON.parseObject(response);
    }

    def doNoPagingRequest(Query query) {
        MockHttpServletRequestBuilder get = get("${getBaseApi()}/no-paging")
        WebUtil.objectToHttpParameters(query.param)
                .forEach({ k, v -> get.param(k, v) })
        def response = mockMvc
                .perform(get)
                .andReturn()
                .response
                .contentAsString;
        return JSON.parseObject(response);
    }
}
