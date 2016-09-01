package org.hsweb.web.service.impl.system;

import org.hsweb.web.service.impl.AbstractTestCase;
import org.hsweb.web.service.system.DataBaseManagerService;
import org.junit.Test;

import javax.annotation.Resource;

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