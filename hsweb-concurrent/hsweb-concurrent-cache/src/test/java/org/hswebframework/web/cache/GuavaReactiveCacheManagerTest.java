package org.hswebframework.web.cache;

import org.hswebframework.web.cache.supports.GuavaReactiveCacheManager;
import org.hswebframework.web.cache.supports.RedisLocalReactiveCacheManager;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


@SpringBootTest(classes = TestApplication.class,args = {
        "--hsweb.cache.type=guava"
})
@RunWith(SpringRunner.class)
public class GuavaReactiveCacheManagerTest {

    @Autowired
    ReactiveCacheManager cacheManager;

    @Test
    public void test(){
        Assert.assertNotNull(cacheManager);
        Assert.assertTrue(cacheManager instanceof GuavaReactiveCacheManager);

        ReactiveCache<String> cache= cacheManager.getCache("test");
        cache.clear()
                .as(StepVerifier::create)
                .verifyComplete();

        cache.flux("test-flux")
                .onCacheMissResume(Flux.just("1","2","3"))
                .as(StepVerifier::create)
                .expectNext("1","2","3")
                .verifyComplete();

        cache.put("test-flux",Flux.just("3","2","1"))
                .as(StepVerifier::create)
                .verifyComplete();

        cache.getFlux("test-flux")
                .as(StepVerifier::create)
                .expectNext("3","2","1")
                .verifyComplete();


        cache.mono("test-mono")
                .onCacheMissResume(Mono.just("1"))
                .as(StepVerifier::create)
                .expectNext("1")
                .verifyComplete();

        cache.put("test-mono",Mono.just("2"))
                .as(StepVerifier::create)
                .verifyComplete();

        cache.getMono("test-mono")
                .as(StepVerifier::create)
                .expectNext("2")
                .verifyComplete();


    }
}