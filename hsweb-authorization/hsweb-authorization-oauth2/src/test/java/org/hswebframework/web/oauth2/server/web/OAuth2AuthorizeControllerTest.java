package org.hswebframework.web.oauth2.server.web;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

public class OAuth2AuthorizeControllerTest {

    @Test
    public void testBuildRedirect() {
        String url = OAuth2AuthorizeController.buildRedirect("http://hsweb.me/callback", Collections.singletonMap("code", "1234"));

        assertEquals(url,"http://hsweb.me/callback?code=1234");
    }

    @Test
    public void testBuildRedirectParam() {
        String url = OAuth2AuthorizeController.buildRedirect("http://hsweb.me/callback?a=b", Collections.singletonMap("code", "1234"));

        assertEquals(url,"http://hsweb.me/callback?a=b&code=1234");
    }

}