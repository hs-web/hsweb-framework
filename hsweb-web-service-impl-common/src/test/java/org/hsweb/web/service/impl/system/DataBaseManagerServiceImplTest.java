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

    @Test
    public void testGetFieldList() throws Exception {
        dataBaseManagerService.getTableList();
    }


}