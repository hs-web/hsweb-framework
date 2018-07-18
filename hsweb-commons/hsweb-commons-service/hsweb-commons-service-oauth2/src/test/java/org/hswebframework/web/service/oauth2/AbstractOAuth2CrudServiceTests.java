package org.hswebframework.web.service.oauth2;

import com.alibaba.fastjson.JSON;
import org.hswebframework.web.BusinessException;
import org.hswebframework.web.authorization.oauth2.client.OAuth2RequestService;
import org.hswebframework.web.authorization.oauth2.client.OAuth2SessionBuilder;
import org.hswebframework.web.authorization.oauth2.client.request.OAuth2Request;
import org.hswebframework.web.authorization.oauth2.client.request.OAuth2Session;
import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AbstractOAuth2CrudServiceTests {

    @Mock
    private OAuth2RequestService requestService;

    @Mock
    private OAuth2SessionBuilder sessionBuilder;

    @Mock
    private OAuth2Session oAuth2Session;

    @InjectMocks
    private TestEntityService testEntityService;

    @Before
    public void init() {
        TestEntity entity = TestEntity.builder().build();
        entity.setBoy(true);
        entity.setCreateTime(new Date());
        entity.setName("test");
        entity.setId("test");

        when(oAuth2Session.request("/test/")).thenReturn(
                createFixedResponseRequest(
                        whenRequest("get", ResponseMessage.ok(PagerResult.of(1, Arrays.asList(entity)))),
                        whenRequest("post", ResponseMessage.ok("test")),
                        whenRequest("patch", ResponseMessage.ok("test"))
                ));

        when(oAuth2Session.request("/test/test")).thenReturn(
                createFixedResponseRequest(
                        whenRequest("get", ResponseMessage.ok(entity))
                        , whenRequest("put", ResponseMessage.ok(1))
                        , whenRequest("delete", ResponseMessage.ok())));

        when(oAuth2Session.request("/test/no-paging")).thenReturn(
                createFixedResponseRequest(
                        whenRequest("get", ResponseMessage.ok(Arrays.asList(entity)))));

        when(oAuth2Session.request("/test/all")).thenReturn(
                createFixedResponseRequest(
                        whenRequest("get", ResponseMessage.ok(Arrays.asList(entity)))));

        when(oAuth2Session.request("/test/single")).thenReturn(
                createFixedResponseRequest(
                        whenRequest("get", ResponseMessage.ok(entity))));

        when(oAuth2Session.request("/test/count")).thenReturn(
                createFixedResponseRequest(
                        whenRequest("get", ResponseMessage.ok(1))));

        when(oAuth2Session.request("/test/ids")).thenReturn(
                createFixedResponseRequest(
                        whenRequest("get", ResponseMessage.ok(Arrays.asList(entity)))));

        when(oAuth2Session.request("/test/batch")).thenReturn(
                createFixedResponseRequest(
                        whenRequest("put", ResponseMessage.error(400, "名称不能为空"))));

        when(sessionBuilder.byClientCredentials()).thenReturn(oAuth2Session);

        when(requestService.create(anyString())).thenReturn(sessionBuilder);

    }

    private OAuth2Request createFixedResponseRequest(OAuth2MethodRequest... requests) {
        return new MockOAuth2Request((method) -> {
            for (OAuth2MethodRequest request : requests) {
                if (request.getMethod().equals(method)) {
                    return new MockOAuth2Response(request.getResponse());
                }
            }
            return new MockOAuth2Response(ResponseMessage.error(404, "not found").toString());
        });
    }

    @Test
    public void testCUD() {
        String id = testEntityService.insert(TestEntity.builder().build());
        Assert.assertNotNull(id);


        TestEntity entity = testEntityService.selectByPk("test");
        Assert.assertNotNull(entity);

        int i = testEntityService.updateByPk("test", entity);
        Assert.assertEquals(i, 1);

         testEntityService.deleteByPk("test");
        Assert.assertEquals(i, 1);

        String saveId = testEntityService.saveOrUpdate(entity);
        Assert.assertNotNull(saveId, id);
        try {
            testEntityService.updateByPk(Arrays.asList(entity));
            Assert.assertTrue(false);
        } catch (BusinessException e) {
            Assert.assertEquals(e.getMessage(), "名称不能为空");
        }

    }

    @Test
    public void testQuery() {

        PagerResult<TestEntity> result = testEntityService.selectPager(new QueryParamEntity().where("name", "test"));
        System.out.println(JSON.toJSONString(result));

        TestEntity entity = testEntityService.selectByPk("test");
        Assert.assertNotNull(entity);
        Assert.assertTrue(entity.isBoy());
        Assert.assertEquals(entity.getName(), "test");

        System.out.println(JSON.toJSONString(entity));

        List<TestEntity> all = testEntityService.select();
        Assert.assertNotNull(all);

        List<TestEntity> noPaging = testEntityService.select(QueryParamEntity.empty());
        Assert.assertNotNull(noPaging);

        int total = testEntityService.count(QueryParamEntity.empty());
        Assert.assertEquals(total, 1);

        total = testEntityService.count();
        Assert.assertEquals(total, 1);

    }

    interface OAuth2MethodRequest {
        String getMethod();

        String getResponse();
    }

    public OAuth2MethodRequest whenRequest(String method, Object json) {
        return new OAuth2MethodRequest() {
            @Override
            public String getMethod() {
                return method;
            }

            @Override
            public String getResponse() {
                return JSON.toJSONString(json);
            }
        };
    }
}