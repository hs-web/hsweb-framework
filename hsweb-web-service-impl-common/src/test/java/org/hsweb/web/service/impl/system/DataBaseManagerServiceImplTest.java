package org.hsweb.web.service.impl.system;

import com.alibaba.fastjson.JSON;
import org.hsweb.web.bean.common.InsertParam;
import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.bean.common.Term;
import org.hsweb.web.bean.common.UpdateParam;
import org.hsweb.web.bean.po.module.Module;
import org.hsweb.web.bean.po.role.RoleModule;
import org.hsweb.web.bean.po.user.User;
import org.hsweb.web.dao.role.RoleMapper;
import org.hsweb.web.dao.role.RoleModuleMapper;
import org.hsweb.web.dao.user.UserMapper;
import org.hsweb.web.service.impl.AbstractTestCase;
import org.hsweb.web.service.module.ModuleMetaService;
import org.hsweb.web.service.module.ModuleService;
import org.hsweb.web.service.system.DataBaseManagerService;
import org.junit.Test;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.webbuilder.sql.support.common.CommonSql;
import org.webbuilder.sql.support.executor.HashMapWrapper;
import org.webbuilder.sql.support.executor.SqlExecutor;

import javax.annotation.Resource;
import java.util.*;

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
        QueryParam queryParam = new QueryParam();
          queryParam.select("username", "password")
                .where("createDate$GT", "2015-12-10");

        userMapper.select(queryParam);
//        User user = userMapper.selectByPk("admin");
//        user.setId("aaaa");
       // userMapper.insert(new InsertParam<>(user));
       // userMapper.update(new UpdateParam<>(user).includes("username"));
    }

    @Test
    public void testGetFieldList() throws Exception {
        RoleModule roleModule = new RoleModule();
        roleModule.setId("aaa");
        roleModule.setActions(Arrays.asList("A","B"));
        roleModule.setModuleId("aaa");
        roleModuleMapper.insert(new InsertParam<>(roleModule));
        roleModuleMapper.update(new UpdateParam<>(roleModule).includes("actions"));
        System.out.println(JSON.toJSONString(roleModuleMapper.select(new QueryParam())));;
    }

    @Test
    public void testExecuteSQL() throws Exception {

    }
}