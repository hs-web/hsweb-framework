package org.hswebframework.web.service.file.oauth2;

import com.alibaba.fastjson.JSON;
import org.hswebframework.web.authorization.oauth2.client.OAuth2RequestService;
import org.hswebframework.web.authorization.oauth2.client.OAuth2SessionBuilder;
import org.hswebframework.web.authorization.oauth2.client.request.OAuth2Request;
import org.hswebframework.web.authorization.oauth2.client.request.OAuth2Session;
import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.entity.file.FileInfoEntity;
import org.hswebframework.web.entity.file.SimpleFileInfoEntity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author zhouhao
 * @since
 */
@RunWith(MockitoJUnitRunner.class)
public class OAuth2FileServiceTest {

    @InjectMocks
    private OAuth2FileService fileService = new OAuth2FileService();

    @Mock
    private OAuth2RequestService auth2RequestService;

    @Mock
    private OAuth2Session oAuth2Session;

    @Before
    public void init() {
        when(oAuth2Session.request("file/download/test")).thenReturn(
                createFixedResponseRequest(
                        whenRequest("get", new ByteArrayInputStream("test".getBytes())))
        );

        when(oAuth2Session.request("file/upload")).thenReturn(
                createFixedResponseRequest(
                        whenRequest("post", ResponseMessage.ok(SimpleFileInfoEntity.builder()
                                .md5("test")
                                .build()).toString()))
        );

        when(oAuth2Session.request("file/upload-static")).thenReturn(
                createFixedResponseRequest(
                        whenRequest("post", ResponseMessage.ok("http://file-server/upload/test.png").toString())));
        OAuth2SessionBuilder builder = mock(OAuth2SessionBuilder.class);
        when(builder.byClientCredentials()).thenReturn(oAuth2Session);

        when(auth2RequestService.create(anyString())).thenReturn(builder);

    }

    @Test
    public void uploadTest() throws IOException {
        String staticFile = fileService.saveStaticFile(new ByteArrayInputStream("test".getBytes()), "test");
        Assert.assertEquals(staticFile, "http://file-server/upload/test.png");
        FileInfoEntity entity = fileService.saveFile(new ByteArrayInputStream("test".getBytes()), "test", "text/plain", "admin");
        Assert.assertNotNull(entity);
        Assert.assertEquals(entity.getMd5(), "test");
    }

    interface OAuth2MethodRequest {
        String getMethod();

        InputStream getResponse();
    }

    public OAuth2MethodRequest whenRequest(String method, String json) {
        return whenRequest(method, new ByteArrayInputStream(json.getBytes()));
    }

    public OAuth2MethodRequest whenRequest(String method, InputStream stream) {
        return new OAuth2MethodRequest() {
            @Override
            public String getMethod() {
                return method;
            }

            @Override
            public InputStream getResponse() {
                return stream;
            }
        };
    }

    private OAuth2Request createFixedResponseRequest(OAuth2MethodRequest... requests) {
        return new MockOAuth2Request((method) -> {
            for (OAuth2MethodRequest request : requests) {
                if (request.getMethod().equals(method)) {
                    return new MockOAuth2Response(request.getResponse());
                }
            }
            return new MockOAuth2Response(new ByteArrayInputStream(ResponseMessage.error(404, "not found").toString().getBytes()));
        });
    }
}