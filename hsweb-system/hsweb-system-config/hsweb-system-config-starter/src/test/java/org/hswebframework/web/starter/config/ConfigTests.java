package org.hswebframework.web.starter.config;

import com.alibaba.fastjson.JSON;
import org.hsweb.ezorm.rdb.executor.SqlExecutor;
import org.hswebframework.web.bean.config.ConfigBean;
import org.hswebframework.web.bean.config.SimpleConfigBean;
import org.hswebframework.web.commons.beans.param.QueryParamBean;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.config.ConfigService;
import org.hswebframework.web.tests.SimpleWebApplicationTests;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.Date;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class ConfigTests extends SimpleWebApplicationTests {

    @Autowired
    private SqlExecutor executor;

    @Autowired
    private ConfigService<QueryParamBean> configService;

    @Test
    public void test() throws SQLException {
        boolean installSuccess = executor.tableExists("s_config");
        Assert.assertTrue(installSuccess);
        ConfigBean configBean = configService.createBean();
        Assert.assertEquals(configBean.getClass(), SimpleConfigBean.class);
        configBean.setId(IDGenerator.RANDOM.generate());
        configBean.addContent("test", 1, "测试");
        configBean.setCreateDate(new Date());
        configBean.setCreatorId("test");

        configService.insert(configBean);

        Assert.assertEquals(configBean.get("test").getNumber(0), 1);
        configBean = configService.selectSingle(new QueryParamBean());
        configBean.addContent("test2", "2", "");
        configService.updateByPk(configBean);

        Assert.assertEquals(configBean.get("test2").getNumber(0).intValue(), 2);
        configBean = configService.selectSingle(new QueryParamBean());
        configService.deleteByPk(configBean.getId());
        Assert.assertEquals(configService.count(new QueryParamBean()), 0);
    }

}
