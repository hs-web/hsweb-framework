package org.hswebframework.web.authorization.starter;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.basic.aop.AopMethodAuthorizeDefinitionParser;
import org.hswebframework.web.authorization.basic.aop.DefaultAopMethodAuthorizeDefinitionParser;
import org.hswebframework.web.authorization.define.AuthorizeDefinition;
import org.hswebframework.web.authorization.define.AuthorizeDefinitionInitializedEvent;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;
import org.hswebframework.web.commons.entity.factory.MapperEntityFactory;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.SimpleGenericEntityController;
import org.hswebframework.web.entity.authorization.PermissionEntity;
import org.hswebframework.web.service.CrudService;
import org.hswebframework.web.service.authorization.PermissionService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.mockito.Mockito.*;

/**
 * @author zhouhao
 * @since 1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class AutoSyncPermissionTest {
    @Mock
    private PermissionService permissionService;

    @InjectMocks
    private AutoSyncPermission autoSyncPermission = new AutoSyncPermission();

    private List<PermissionEntity> newPermissionEntity = new ArrayList<>();

    @Before
    public void init() throws NoSuchMethodException {
        when(permissionService.selectByPk(anyString())).thenReturn(null);
        when(permissionService.insert(any())).then(invocationOnMock -> {
            newPermissionEntity.add(invocationOnMock.getArgumentAt(0, PermissionEntity.class));
            return "new Id";
        });
        autoSyncPermission.setEntityFactory(new MapperEntityFactory());
    }

    @Test
    public void test() throws NoSuchMethodException {
        AopMethodAuthorizeDefinitionParser parser = new DefaultAopMethodAuthorizeDefinitionParser();
        List<AuthorizeDefinition> definition = Arrays.stream(TestController.class.getMethods())
                .map(method -> parser.parse(TestController.class, method))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());


        autoSyncPermission.onApplicationEvent(new AuthorizeDefinitionInitializedEvent(definition));

        Assert.assertTrue(!newPermissionEntity.isEmpty());
        PermissionEntity permissionEntity = newPermissionEntity.get(0);
        Assert.assertEquals(permissionEntity.getId(), "test");
        Assert.assertEquals(permissionEntity.getName(), "测试权限");
        Assert.assertTrue(!permissionEntity.getActions().isEmpty());

        Assert.assertEquals(permissionEntity.getOptionalFields().size(), 3);
    }

    @Authorize(permission = "test", description = "测试权限")
    @Api(value = "测试", tags = "测试")
    public static class TestController implements SimpleGenericEntityController<TestEntity, String, QueryParamEntity> {

        @Override
        public CrudService<TestEntity, String> getService() {
            return null;
        }
    }

    @Data
    public static class TestEntity extends SimpleGenericEntity<String> {
        @ApiModelProperty("姓名")
        private String name;

        @ApiModelProperty("用户名")
        private String username;

        @ApiModelProperty(value = "密码", hidden = true)
        private String password;
    }
}