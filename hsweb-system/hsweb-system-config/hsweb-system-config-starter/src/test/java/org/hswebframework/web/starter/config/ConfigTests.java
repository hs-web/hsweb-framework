package org.hswebframework.web.starter.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.hswebframework.ezorm.rdb.executor.SqlExecutor;
import org.hswebframework.web.entity.config.ConfigEntity;
import org.hswebframework.web.entity.config.SimpleConfigEntity;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.config.ConfigService;
import org.hswebframework.web.tests.SimpleWebApplicationTests;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

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
    private ConfigService configService;

    @After
    public void clear() throws SQLException {
        executor.delete("delete from s_config");
    }

    @Test
    public void testMvc() throws Exception {
        //创建bean
        ConfigEntity configBean = configService.createEntity();
        Assert.assertEquals(configBean.getClass(), SimpleConfigEntity.class);
        configBean.setId(IDGenerator.RANDOM.generate());
        configBean.addContent("test", 1, "测试");
        configBean.setCreateTime(System.currentTimeMillis());
        configBean.setCreatorId("test");
        String jsonStr = JSON.toJSONString(configBean);

        JSONObject jsonObject = testPost("/config")
                .setUp(builder -> builder.accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonStr)
                )
                .exec().resultAsJson();
        //{data:id,code:200}
        Assert.assertEquals(jsonObject.getString("result"), configBean.getId());

        JSONObject getRes = testGet("/config/" + configBean.getId()).exec().resultAsJson();
        Assert.assertEquals(getRes
                .getObject("result", SimpleConfigEntity.class)
                .get("test")
                .getNumber(0).intValue(), 1);

        getRes = testGet("/config").setUp(builder ->
                builder.param("terms[0].column", "id")
                        .param("terms[0].value", configBean.getId())
        ).exec().resultAsJson();
        Assert.assertEquals(getRes.getJSONObject("result").getJSONArray("data")
                .getObject(0, SimpleConfigEntity.class)
                .get("test")
                .getNumber(0).intValue(), 1);

        jsonObject = testPut("/config/" + configBean.getId())
                .setUp(builder -> builder.accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonStr)
                )
                .exec().resultAsJson();
        Assert.assertEquals(200, jsonObject.get("status"));
    }

    @Test
    public void test() throws SQLException {
        //判断是否安装成功
        boolean installSuccess = executor.tableExists("s_config");
        Assert.assertTrue(installSuccess);
        //创建bean
        ConfigEntity configBean = configService.createEntity();
        Assert.assertEquals(configBean.getClass(), SimpleConfigEntity.class);
        configBean.setId(IDGenerator.RANDOM.generate());
        configBean.addContent("test", 1, "测试");
        configBean.setCreateTime(System.currentTimeMillis());
        configBean.setCreatorId("test");
        //test insert
        configService.insert(configBean);
        Assert.assertEquals(configBean.get("test").getNumber(0), 1);
        configBean = configService.selectSingle(QueryParamEntity.empty());
        configBean.addContent("test2", "2", "");
        //test update
        Assert.assertEquals(configService.updateByPk(configBean.getId(), configBean), 1);
        Assert.assertEquals(configBean.get("test2").getNumber(0).intValue(), 2);
        configBean = configService.selectSingle(QueryParamEntity.empty());
        //test delete
        configService.deleteByPk(configBean.getId());
        Assert.assertEquals(configService.count(QueryParamEntity.empty()), 0);
    }

}
