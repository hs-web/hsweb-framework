package org.hswebframework.web.tests

import com.alibaba.fastjson.JSONObject
import spock.lang.Specification

/**
 * @author zhouhao
 * @since 3.0.2
 */
class HswebCrudWebApiSpecificationTest extends HswebCrudWebApiSpecification {

    @Override
    protected String getBaseApi() {
        return "/test"
    }

    def "测试初始化"() {
        expect:
        mockMvc != null
        context != null
    }

    def "测试新增"() {
        given:
        def response = doAddRequest(JSONObject.toJSONString([name: "test"]));
        expect:
        response != null
        response.status == 200 || response.status == 201
    }

    def "测试修改"() {
        given:
        def response = doUpdateRequest("test", JSONObject.toJSONString([name: "test"]));
        expect:
        response != null
        response.status == 200
    }

    def "测试删除"() {
        given:
        def response = doDeleteRequest("test");
        expect:
        response != null
        response.status == 200
    }

    def "测试查询"() {
        given:
        def response = doQueryRequest(createQuery().where("id", "1234"));
        expect:
        response != null
        response.status == 200
    }

    def "测试根据id查询"() {
        given:
        def response = doGetRequest("1");
        expect:
        response != null
        response.status == 200
    }

    def "测试根据id集合查询"() {
        given:
        def response = doQueryByIdsRequest("1,2,3,4");
        expect:
        response != null
        response.status == 200
    }

    def "测试查询总数"() {
        given:
        def response = doCountRequest(createQuery().where("id", "1234"));
        expect:
        response != null
        response.status == 200
    }


    def "测试不分页查询"() {
        given:
        def response = doNoPagingRequest(createQuery().where("id", "1234"));
        expect:
        response != null
        response.status == 200
    }

}
