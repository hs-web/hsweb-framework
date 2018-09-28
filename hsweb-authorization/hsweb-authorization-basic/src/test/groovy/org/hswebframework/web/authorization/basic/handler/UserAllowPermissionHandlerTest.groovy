package org.hswebframework.web.authorization.basic.handler

import org.hswebframework.web.authorization.Authentication
import org.hswebframework.web.authorization.AuthenticationManager
import org.hswebframework.web.authorization.TestApplication
import org.hswebframework.web.authorization.basic.define.EmptyAuthorizeDefinition
import org.hswebframework.web.authorization.define.AuthorizeDefinition
import org.hswebframework.web.authorization.define.AuthorizingContext
import org.hswebframework.web.authorization.define.HandleType
import org.hswebframework.web.authorization.listener.event.AuthorizingHandleBeforeEvent
import org.hswebframework.web.authorization.simple.PlainTextUsernamePasswordAuthenticationRequest
import org.hswebframework.web.boost.aop.context.MethodInterceptorContext
import org.hswebframework.web.boost.aop.context.MethodInterceptorHolder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.web.WebAppConfiguration
import spock.lang.Specification

/**
 * @author zhouhao
 * @since 3.0.1
 */
@WebAppConfiguration
@ContextConfiguration
@SpringBootTest(classes = [TestApplication.class], properties = ["classpath:application.yml"])
class UserAllowPermissionHandlerTest extends Specification {

    @Autowired
    UserAllowPermissionHandler handler;

    @Autowired
    private AuthenticationManager manager;

    def createMethodInterceptorContext(TestController controller, String name) {
        return new MethodInterceptorHolder(
                "test"
                , TestController.class.getMethod(name)
                , controller
                , new HashMap<String, Object>())
                .createParamContext()
    }

    def "Test"() {
        setup:
        def authentication = manager.authenticate(new PlainTextUsernamePasswordAuthenticationRequest("admin", "admin"));
        def definition = EmptyAuthorizeDefinition.instance;
        def controller = new TestController();
        def context = createMethodInterceptorContext(controller, "query");
        def authorizingContext = new AuthorizingContext(
                authentication: authentication
                , definition: definition
                , paramContext: context);
        def event = new AuthorizingHandleBeforeEvent(authorizingContext, HandleType.RBAC);
        handler.handEvent(event);
        expect:
        authentication != null
        event.isAllow()

    }
}
