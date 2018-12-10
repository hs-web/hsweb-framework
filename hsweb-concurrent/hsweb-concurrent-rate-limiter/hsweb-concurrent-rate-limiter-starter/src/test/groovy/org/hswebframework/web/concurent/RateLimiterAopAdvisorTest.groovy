package org.hswebframework.web.concurent

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

/**
 * @author zhouhao
 * @since 3.0.4
 */
@SpringBootTest(classes = TestApplication.class)
@ContextConfiguration
class RateLimiterAopAdvisorTest extends Specification {

    @Autowired
    public TestService testService;

    def "测试限流"() {
        TestService.counter.set(0);
        given:
        testService.test();
        def timeoutException;
        try {
            testService.test();
        } catch (Exception e) {
            timeoutException = e;
        }
        expect:
        TestService.counter.get() == 1
        timeoutException != null
    }

    def "测试指定key限流"() {
        TestService.counter.set(0);
        given:
        testService.test("test");
        def timeoutException;
        try {
            testService.test("test");
        } catch (Exception e) {
            timeoutException = e;
        }
        expect:
        TestService.counter.get() == 1
        timeoutException != null
    }
}
