package org.hswebframework.web.oauth2.server;

import org.junit.Test;

import static org.junit.Assert.*;

public class OAuth2ClientTest {

    @Test
    public void test(){
        OAuth2Client client=new OAuth2Client();

        client.setRedirectUrl("http://hsweb.me/callback");

        client.validateRedirectUri("http://hsweb.me/callback");

        client.validateRedirectUri("http://hsweb.me/callback?a=1&n=1");

    }
}