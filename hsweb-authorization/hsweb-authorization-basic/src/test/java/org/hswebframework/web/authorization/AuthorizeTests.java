package org.hswebframework.web.authorization;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.*;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.RequiresDataAccess;
import org.hswebframework.web.authorization.basic.aop.AopMethodAuthorizeDefinitionParser;
import org.hswebframework.web.authorization.basic.aop.DefaultAopMethodAuthorizeDefinitionParser;
import org.hswebframework.web.authorization.basic.handler.DefaultAuthorizingHandler;
import org.hswebframework.web.authorization.basic.handler.access.DefaultDataAccessController;
import org.hswebframework.web.authorization.define.AuthorizeDefinition;
import org.hswebframework.web.authorization.define.AuthorizingContext;
import org.hswebframework.web.authorization.define.Phased;
import org.hswebframework.web.authorization.simple.*;
import org.hswebframework.web.boost.aop.context.MethodInterceptorContext;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthorizeTests {

    @Mock
    private MethodInterceptorContext queryById;
    @Mock
    private MethodInterceptorContext dynamicQuery;

    @Mock
    private Authentication authentication;

    AopMethodAuthorizeDefinitionParser parser = new DefaultAopMethodAuthorizeDefinitionParser();

    @Before
    public void init() throws NoSuchMethodException {
        TestClass testClass = new TestClass();

        QueryParamEntity entity = new QueryParamEntity();
        entity.where("id", "admin").or("name", "admin");

        User user = User.builder().name("test").id("test")
                .orgId("400000")
                .password("admin").salt("1234").build();

        //mock MethodInterceptorContext
        when(queryById.getMethod()).thenReturn(TestClass.class.getMethod("queryById", String.class));
        when(queryById.getTarget()).thenReturn(testClass);
        when(queryById.getParameter("id")).thenReturn(Optional.of("test"));
        when(queryById.getParams()).thenReturn(Collections.singletonMap("id", "test"));
        when(queryById.getInvokeResult()).thenReturn(ResponseMessage.ok(user));


        //mock MethodInterceptorContext
        when(dynamicQuery.getMethod()).thenReturn(TestClass.class.getMethod("dynamicQuery", QueryParamEntity.class));
        when(dynamicQuery.getTarget()).thenReturn(testClass);
        when(dynamicQuery.getParams()).thenReturn(Collections.singletonMap("paramEntity", entity));
        when(dynamicQuery.getParameter("paramEntity")).thenReturn(Optional.of(entity));


        //过滤字段
        AbstractDataAccessConfig fieldFilter = new SimpleFieldFilterDataAccessConfig("password", "salt");
        fieldFilter.setAction(Permission.ACTION_QUERY);

        SimpleFiledScopeDataAccessConfig filedScope = new SimpleFiledScopeDataAccessConfig();
        filedScope.setAction(Permission.ACTION_QUERY);
        filedScope.setField("orgId");
        filedScope.setScopeType("org");
        filedScope.setScope(Collections.singleton("400000"));

        //mock authentication
        when(authentication.getUser()).thenReturn(SimpleUser.builder().id("admin").name("admin").build());
        when(authentication.getPermissions()).thenReturn(Arrays.asList(SimplePermission.builder()
                .id("test")
                .dataAccesses(new HashSet<>(Arrays.asList(fieldFilter, filedScope)))

                .actions(new HashSet<>(Arrays.asList(Permission.ACTION_QUERY, Permission.ACTION_UPDATE))).build()));

    }


    @Test
    public void testParseAuthorizeDefinition() {
        AuthorizeDefinition definition = parser.parse(queryById.getTarget().getClass(), queryById.getMethod());

        Assert.assertNotNull(definition);
        Assert.assertEquals(definition.getPermissions().size(), 1);
        Assert.assertEquals(definition.getPermissions().iterator().next(), "test");
        Assert.assertEquals(definition.getActions().iterator().next(), Permission.ACTION_QUERY);
    }

    @Test
    public void testAuthorizingHandler() {
        DefaultAuthorizingHandler handler = new DefaultAuthorizingHandler();

        AuthorizeDefinition definition = parser.parse(queryById.getTarget().getClass(), queryById.getMethod());

        AuthorizingContext authorizingContext = new AuthorizingContext();
        authorizingContext.setAuthentication(authentication);
        authorizingContext.setDefinition(definition);
        authorizingContext.setParamContext(queryById);

        handler.handRBAC(authorizingContext);


    }

    /**
     * 测试数据权限控制s
     */
    @Test
    public void testDynamicQueryDataAccessHandler() {

        DefaultAuthorizingHandler handler = new DefaultAuthorizingHandler();
        DefaultDataAccessController controller = new DefaultDataAccessController();
        handler.setDataAccessController(controller);


        AuthorizeDefinition definition = parser.parse(dynamicQuery.getTarget().getClass(), dynamicQuery.getMethod());

        //获取到请求参数
        QueryParamEntity entity = dynamicQuery.<QueryParamEntity>getParameter("paramEntity").orElseThrow(NullPointerException::new);
        System.out.println(JSON.toJSONString(entity, SerializerFeature.PrettyFormat));

        AuthorizingContext authorizingContext = new AuthorizingContext();
        authorizingContext.setAuthentication(authentication);
        authorizingContext.setDefinition(definition);
        authorizingContext.setParamContext(dynamicQuery);

        handler.handleDataAccess(authorizingContext);

        System.out.println(JSON.toJSONString(entity, SerializerFeature.PrettyFormat));

        Assert.assertTrue(entity.getExcludes().size() == 2);
        Assert.assertTrue(entity.getTerms().size() == 2);
        Assert.assertTrue(entity.getTerms().get(1).getTerms().size() == 2);
    }

    /**
     * 测试数据权限控制s
     */
    @Test
    public void testGetDataAccessHandler() {

        DefaultAuthorizingHandler handler = new DefaultAuthorizingHandler();
        DefaultDataAccessController controller = new DefaultDataAccessController();
        handler.setDataAccessController(controller);


        AuthorizeDefinition definition = parser.parse(queryById.getTarget().getClass(), queryById.getMethod());

        //响应结果
        Object response = queryById.getInvokeResult();

        System.out.println(JSON.toJSONString(response, SerializerFeature.PrettyFormat));

        AuthorizingContext authorizingContext = new AuthorizingContext();
        authorizingContext.setAuthentication(authentication);
        authorizingContext.setDefinition(definition);
        authorizingContext.setParamContext(queryById);

        handler.handleDataAccess(authorizingContext);

        System.out.println(JSON.toJSONString(response, SerializerFeature.PrettyFormat));
        Assert.assertTrue(response instanceof ResponseMessage);
        Assert.assertTrue(((User) ((ResponseMessage) response).getResult()).getPassword() == null);
        Assert.assertTrue(((User) ((ResponseMessage) response).getResult()).getSalt() == null);
    }

    @Authorize(permission = "test")
    public static class TestClass implements TestClassSuper {

        public ResponseMessage<User> queryById(String id) {
            return ResponseMessage.ok();
        }

        @Authorize(action = Permission.ACTION_QUERY)
        @RequiresDataAccess
        public void dynamicQuery(QueryParamEntity paramEntity) {
            System.out.println(JSON.toJSON(paramEntity));
        }

    }

    public interface TestClassSuper {

        @Authorize(action = Permission.ACTION_QUERY, phased = Phased.after, dataAccess = @RequiresDataAccess)
        default ResponseMessage<User> queryById(String id) {
            return ResponseMessage.ok();
        }

        @Authorize(action = Permission.ACTION_QUERY)
        @RequiresDataAccess
        default void dynamicQuery(QueryParamEntity paramEntity) {
            System.out.println(JSON.toJSON(paramEntity));
        }

    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class User {
        private String id;

        private String name;

        private String password;

        private String salt;

        private String orgId;

    }
}
