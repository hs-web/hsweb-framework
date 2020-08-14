package org.hswebframework.web.system.authorization.defaults.service.reactive;

import org.hswebframework.web.crud.configuration.EasyormConfiguration;
import org.hswebframework.web.crud.configuration.JdbcSqlExecutorConfiguration;
import org.hswebframework.web.crud.configuration.R2dbcSqlExecutorConfiguration;
import org.hswebframework.web.system.authorization.defaults.configuration.AuthorizationServiceAutoConfiguration;
import org.hswebframework.web.system.authorization.defaults.configuration.AuthorizationWebAutoConfiguration;
import org.hswebframework.web.system.authorization.defaults.webflux.WebFluxPermissionController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.r2dbc.R2dbcTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@RunWith(SpringRunner.class)
@WebFluxTest(WebFluxPermissionController.class)
@ImportAutoConfiguration(value = {
        AuthorizationWebAutoConfiguration.class,
        AuthorizationServiceAutoConfiguration.class,
        EasyormConfiguration.class,
        R2dbcSqlExecutorConfiguration.class, R2dbcAutoConfiguration.class,
        R2dbcTransactionManagerAutoConfiguration.class
},exclude = {
        JdbcSqlExecutorConfiguration.class,
        TransactionAutoConfiguration.class
})
@EnableTransactionManagement(proxyTargetClass = true)
@EnableAutoConfiguration
public class WebFluxPermissionControllerTest {

    @Autowired
    WebTestClient client;

    @Test
    public void test(){
        byte[] data=client.get()
                .uri("/permission/_count")
                //.contentType(MediaType.APPLICATION_JSON)
//                .body(Mono.just(PermissionEntity
//                        .builder()
//                        .name("test")
//                        .build()),PermissionEntity.class)
                .exchange()
                .expectBody()
                .returnResult()
                .getResponseBody();
        System.out.println(new String(data));
    }

}
