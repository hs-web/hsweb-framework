package org.hsweb.web.service.impl.form;

import org.hsweb.web.bean.po.form.Form;
import org.hsweb.web.service.impl.AbstractTestCase;
import org.hsweb.web.utils.RandomUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.webbuilder.sql.DataBase;
import org.webbuilder.sql.param.insert.InsertParam;
import org.webbuilder.sql.param.query.QueryParam;
import org.webbuilder.sql.support.common.CommonSql;
import org.webbuilder.sql.support.executor.SqlExecutor;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;

/**
 * Created by zhouhao on 16-4-20.
 */
@Transactional
@Rollback
public class FormServiceImplTest extends AbstractTestCase {

    @Resource
    private FormServiceImpl formService;

    @Resource
    private DataBase dataBase;

    @Resource
    private SqlExecutor sqlExecutor;

    private Form form;

    @Before
    public void setup() throws Exception {
        sqlExecutor.exec(new CommonSql("drop table if exists s_form"));
        sqlExecutor.exec(new CommonSql("create table s_form\n" +
                "(\n" +
                "  u_id        varchar(256) not null,\n" +
                "  name        varchar(256) not null,\n" +
                "  html        clob,\n" +
                "  meta        clob,\n" +
                "  config      clob,\n" +
                "  version     number(32),\n" +
                "  using       int,\n" +
                "  create_date date not null,\n" +
                "  update_date date,\n" +
                "  remark      varchar2(200)\n" +
                ")"));

        form = new Form();
        form.setName("test_form");
        form.setCreate_date(new Date());
        form.setMeta("{\"ehFpAHYTFXZcPBBzyRmSBtmQSWdjMXxM\":[{\"key\":\"name\",\"value\":\"name\",\"describe\":\"名称\",\"_id\":45,\"_uid\":45,\"_state\":\"modified\"},{\"key\":\"comment\",\"value\":\"test\",\"describe\":\"字段描述\",\"_id\":46,\"_uid\":46,\"_state\":\"modified\"},{\"key\":\"javaType\",\"value\":\"string\",\"describe\":\"java类型\",\"_id\":47,\"_uid\":47},{\"key\":\"dataType\",\"value\":\"varchar2(32)\",\"describe\":\"数据库类型\",\"_id\":48,\"_uid\":48},{\"key\":\"_meta\",\"value\":\"textbox\",\"describe\":\"控件类型\",\"_id\":49,\"_uid\":49},{\"key\":\"validator-list\",\"value\":\"[]\",\"describe\":\"验证器\",\"_id\":50,\"_uid\":50},{\"key\":\"domProperty\",\"value\":\"[]\",\"describe\":\"其他控件配置\",\"_id\":51,\"_uid\":51}]}");
        form.setU_id(RandomUtil.randomChar());
        formService.insert(form);
    }

    @Test
    public void testDeploy() throws Exception {
        //部署
        formService.deploy(form.getU_id());
        dataBase.getTable("test_form").createInsert()
                .insert(new InsertParam().value("name", "张三"));

        Map<String, Object> data = dataBase.getTable("test_form")
                .createQuery().single(new QueryParam().where("name$LIKE", "张三"));

        Assert.assertEquals("张三", data.get("name"));
    }
}