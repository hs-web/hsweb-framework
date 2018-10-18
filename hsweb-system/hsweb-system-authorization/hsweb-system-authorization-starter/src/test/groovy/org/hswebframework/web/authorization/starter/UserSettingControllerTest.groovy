package org.hswebframework.web.authorization.starter

import com.alibaba.fastjson.JSON
import org.hswebframework.web.authorization.Authentication
import org.hswebframework.web.authorization.AuthenticationInitializeService
import org.hswebframework.web.authorization.access.DataAccessConfig
import org.hswebframework.web.tests.HswebCrudWebApiSpecification
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.ResultHandler
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Shared
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

/**
 * @author zhouhao
 * @since 3.0
 */
class UserSettingControllerTest extends HswebCrudWebApiSpecification {

    @Autowired
    private AuthenticationInitializeService initializeService;

    @Override
    protected String getBaseApi() {
        return "/autz-setting"
    }


    def "Add Permission"() {
        def permissions = [
                [
                        "id"                    : "user",
                        "name"                  : "用户管理",
                        "actions"               : [["action": "query", "describe": "查询"], ["action": "update", "describe": "修改"]],
                        "supportDataAccessTypes": [DataAccessConfig.DefaultType.DENY_FIELDS]
                ],
                [
                        "id"     : "role",
                        "name"   : "角色管理",
                        "actions": [["action": "query", "describe": "查询"], ["action": "get", "describe": "查看详情"]]

                ]
        ]
        permissions.forEach({ permission ->
            //添加权限
            mockMvc.perform(patch("/permission")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(JSON.toJSONString(permission)))
                    .andDo({ result -> println result.response.contentAsString })
                    .andExpect(status().is(200))
        })
    }

    def "Add User"() {
        //添加用户先
        String result = mockMvc
                .perform(
                post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                        """
                        {
                            "name":"admin",
                            "username":"admin",
                            "password":"admin"
                        }
                        """
                ))
                .andDo({ result -> println result.response.contentAsString })
                .andExpect(status().is(201))
                .andReturn()
                .getResponse()
                .getContentAsString()
        return JSON.parseObject(result).getString("result")
    }

    def "Add User Authentication Setting"() {
        setup:
        "Add Permission"()
        def userId = "Add User"()
        //添加用户权限
        doAddRequest(JSON.toJSONString
                ([
                        type      : "user", //设置类型:user
                        settingFor: userId, //设置给具体的user
                        describe  : "测试",
                        details   :
                                [
                                        [
                                                permissionId: "user", //赋予user权限
                                                actions     : ["query", "update"],
                                                status      : 1,
                                                dataAccesses: [ //数据权限,控制查询的时候不能查询password字段
                                                                [
                                                                        type  : DataAccessConfig.DefaultType.DENY_FIELDS,
                                                                        action: "query",
                                                                        config: """{"fields": ["password"]}"""
                                                                ]
                                                ]
                                        ],
                                        [
                                                permissionId: "role", //赋予role权限
                                                actions     : ["query", "get"],
                                                status      : 1
                                        ]
                                ],
                        menus     :
                                [
                                        [
                                                menuId: "user-menu"
                                        ],
                                        [
                                                menuId: "role-menu"
                                        ]
                                ]
                ]))

        expect:
        userId != null
        def autz = initializeService.initUserAuthorization(userId)
        autz != null
        autz.hasPermission("user", "query")
        autz.hasPermission("role", "query", "get")
        autz.getPermission("user")
                .map({ per -> per.findDenyFields("query") })
                .map({ fields -> fields.contains("password") })
                .orElse(false)
    }

}
