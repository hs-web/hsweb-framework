package org.hsweb.web.service.impl.system;

import com.alibaba.fastjson.JSON;
import org.hsweb.ezorm.executor.SqlExecutor;
import org.hsweb.web.bean.common.DeleteParam;
import org.hsweb.web.bean.common.InsertParam;
import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.bean.common.UpdateParam;
import org.hsweb.web.bean.po.role.RoleModule;
import org.hsweb.web.bean.po.user.User;
import org.hsweb.web.dao.role.RoleModuleMapper;
import org.hsweb.web.dao.user.UserMapper;
import org.hsweb.web.service.impl.AbstractTestCase;
import org.hsweb.web.service.module.ModuleMetaService;
import org.hsweb.web.service.system.DataBaseManagerService;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by zhouhao on 16-4-21.
 */
public class DataBaseManagerServiceImplTest extends AbstractTestCase {

    @Resource
    private DataBaseManagerService dataBaseManagerService;
    @Resource
    private SqlExecutor sqlExecutor;

    @Resource
    private ModuleMetaService moduleMetaService;

    @Resource
    private UserMapper userMapper;

    @Resource
    private RoleModuleMapper roleModuleMapper;

    @Test
    public void test() throws Exception {
        QueryParam queryParam = QueryParam.build()
         .select("username", "password")
                .where("createDate$GT", "2015-12-10")
        .orderBy("u_id ;delete * from user;").asc();

        userMapper.select(queryParam);

    }

    @Test
    public void testGetFieldList() throws Exception {
        RoleModule roleModule = new RoleModule();
        roleModule.setId("aaa");
        roleModule.setActions(Arrays.asList("A", "B"));
        roleModule.setModuleId("aaa");
        roleModuleMapper.insert(new InsertParam<>(roleModule));
        roleModuleMapper.update(new UpdateParam<>(roleModule).includes("actions"));
        System.out.println(JSON.toJSONString(roleModuleMapper.select(new QueryParam())));
    }

    @Test
    public void testExecuteSQL() throws Exception {

    }

}