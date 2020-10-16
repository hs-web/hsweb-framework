package org.hswebframework.web.oauth2.configuration;

import org.hswebframework.web.oauth2.ReactiveTestApplication;
import org.hswebframework.web.oauth2.server.OAuth2ClientManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ReactiveTestApplication.class)
public class OAuth2ClientManagerAutoConfigurationTest {

    @Autowired
    OAuth2ClientManager clientManager;

    @Test
    public void test(){
        assertNotNull(clientManager);
    }
}