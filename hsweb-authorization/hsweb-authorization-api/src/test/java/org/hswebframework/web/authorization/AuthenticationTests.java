package org.hswebframework.web.authorization;

import org.hswebframework.web.authorization.builder.AuthenticationBuilder;
import org.hswebframework.web.authorization.exception.UnAuthorizedException;
import org.hswebframework.web.authorization.simple.builder.SimpleAuthenticationBuilder;
import org.hswebframework.web.authorization.simple.builder.SimpleDataAccessConfigBuilderFactory;
import org.hswebframework.web.authorization.token.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.*;

public class AuthenticationTests {

    private AuthenticationBuilder builder;

    @Before
    public void setup() {
        SimpleDataAccessConfigBuilderFactory builderFactory = new SimpleDataAccessConfigBuilderFactory();

        builderFactory.init();

        builder = new SimpleAuthenticationBuilder(builderFactory);
    }

    /**
     * 测试初始化基本的权限信息
     */
    @Test
    public void testInitUserRoleAndPermission() {
        Authentication authentication = builder.user("{\"id\":\"admin\",\"username\":\"admin\",\"name\":\"Administrator\",\"type\":\"default\"}")
                .role("[{\"id\":\"admin-role\",\"name\":\"admin\"}]")
                .permission("[{\"id\":\"user-manager\",\"actions\":[\"query\",\"get\",\"update\"]" +
                        ",\"dataAccesses\":[{\"action\":\"query\",\"field\":\"test\",\"fields\":[\"1\",\"2\",\"3\"],\"scopeType\":\"CUSTOM_SCOPE\",\"type\":\"DENY_FIELDS\"}]}]")
                .build();

        //test user
        assertEquals(authentication.getUser().getId(), "admin");
        assertEquals(authentication.getUser().getUsername(), "admin");
        assertEquals(authentication.getUser().getName(), "Administrator");
        assertEquals(authentication.getUser().getType(), "default");

        //test role
        assertNotNull(authentication.getRole("admin-role").orElse(null));
        assertEquals(authentication.getRole("admin-role").get().getName(), "admin");
        assertTrue(authentication.hasRole("admin-role"));


        //test permission
        assertEquals(authentication.getPermissions().size(), 1);
        assertTrue(authentication.hasPermission("user-manager"));
        assertTrue(authentication.hasPermission("user-manager", "get"));
        assertTrue(!authentication.hasPermission("user-manager", "delete"));

        boolean has = AuthenticationPredicate.has("permission:user-manager")
                .or(AuthenticationPredicate.role("admin-role"))
                .test(authentication);

        Assert.assertTrue(has);
        has = AuthenticationPredicate.has("permission:user-manager:test")
                .and(AuthenticationPredicate.role("admin-role"))
                .test(authentication);
        Assert.assertFalse(has);

        has = AuthenticationPredicate.has("permission:user-manager:get and role:admin-role")
                .test(authentication);
        Assert.assertTrue(has);

        has = AuthenticationPredicate.has("permission:user-manager:test or role:admin-role")
                .test(authentication);
        Assert.assertTrue(has);

        //获取数据权限配置
        Set<String> fields = authentication.getPermission("user-manager")
                .map(permission -> permission.findDenyFields(Permission.ACTION_QUERY))
                .orElseGet(Collections::emptySet);

        Assert.assertEquals(fields.size(), 3);
        System.out.println(fields);

    }

    /**
     * 测试设置获取当前登录用户
     */
    @Test
    public void testGetSetCurrentUser() {
        Authentication authentication = builder.user("{\"id\":\"admin\",\"username\":\"admin\",\"name\":\"Administrator\",\"type\":\"default\"}")
                .build();

        //初始化权限管理器,用于获取用户的权限信息
        AuthenticationManager authenticationManager = new AuthenticationManager() {
            @Override
            public Authentication authenticate(AuthenticationRequest request) {
                return null;
            }

            @Override
            public Authentication getByUserId(String userId) {
                if (userId.equals("admin")) {
                    return authentication;
                }
                return null;
            }

            @Override
            public Authentication sync(Authentication authentication) {
                return authentication;
            }
        };
        AuthenticationHolder.addSupplier(new UserTokenAuthenticationSupplier(authenticationManager));

        //绑定用户token
        UserTokenManager userTokenManager = new DefaultUserTokenManager();
        UserToken token = userTokenManager.signIn("test", "token-test", "admin", -1);
        UserTokenHolder.setCurrent(token);

        //获取当前登录用户
        Authentication current = Authentication.current().orElseThrow(UnAuthorizedException::new);
        Assert.assertEquals(current.getUser().getId(), "admin");


    }
}