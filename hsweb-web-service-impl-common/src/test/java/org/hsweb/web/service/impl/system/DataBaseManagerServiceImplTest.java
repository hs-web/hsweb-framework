package org.hsweb.web.service.impl.system;

import org.hsweb.web.bean.common.InsertParam;
import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.bean.common.Term;
import org.hsweb.web.bean.common.UpdateParam;
import org.hsweb.web.bean.po.module.Module;
import org.hsweb.web.bean.po.user.User;
import org.hsweb.web.dao.user.UserMapper;
import org.hsweb.web.service.impl.AbstractTestCase;
import org.hsweb.web.service.module.ModuleMetaService;
import org.hsweb.web.service.module.ModuleService;
import org.hsweb.web.service.system.DataBaseManagerService;
import org.junit.Test;
import org.webbuilder.sql.support.common.CommonSql;
import org.webbuilder.sql.support.executor.HashMapWrapper;
import org.webbuilder.sql.support.executor.SqlExecutor;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Test
    public void test() throws Exception {
        QueryParam queryParam = new QueryParam();
        Term term = queryParam.select("username", "password")
                .where("createDate$GT", "2015-12-10")
                .nest();
        term = term.nest("username", "admin").or("username", "test").nest();
        term = term.nest();
        term.orNest("status$IN", "1,2,3").and("status$LT", "0");
        term.nest("status$IN", "2,3,4").and("status$LT", "1");
        term.and("username$EMPTY", true);
        queryParam.orderBy("create_date").desc();
        queryParam.orderBy("status").asc();
        userMapper.select(queryParam);
        User user = userMapper.selectByPk("admin");
        user.setUId("aaaa");
        userMapper.insert(new InsertParam<>(user));
        userMapper.update(new UpdateParam<>(user).includes("username"));
    }

    @Test
    public void testGetFieldList() throws Exception {
        moduleMetaService.selectByKeyAndRoleId("test", "userRole");

    }

    @Test
    public void testExecuteSQL() throws Exception {

    }
}