package org.hsweb.web.service.impl.system;

import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.bean.po.module.Module;
import org.hsweb.web.dao.user.UserMapper;
import org.hsweb.web.service.impl.AbstractTestCase;
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
    private UserMapper userMapper;

    @Test
    public void testGetTableNameList() throws Exception {
        QueryParam queryParam = new QueryParam();
//        queryParam.nest("name$LIKE", "%admin")
//                .or("name","1")
//                .nestOr("name","1")
//                .and("name","2");
        queryParam.select("username", "password")
                .where("create_date$GT", "2015-12-10")
                .nest("username","admin").or("username", "test");
        userMapper.select(queryParam);
    }

    @Test
    public void testGetFieldList() throws Exception {

    }

    @Test
    public void testExecuteSQL() throws Exception {

    }
}