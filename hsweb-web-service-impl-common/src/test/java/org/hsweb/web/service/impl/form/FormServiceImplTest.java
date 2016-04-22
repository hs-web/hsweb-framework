package org.hsweb.web.service.impl.form;

import org.hsweb.web.bean.po.form.Form;
import org.hsweb.web.service.form.FormService;
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
    protected FormService formService;

    @Resource
    protected DataBase dataBase;

    @Resource
    protected SqlExecutor sqlExecutor;

    protected Form form;

    private String[] meta = {
            "{" +
                    "\"id1\":[" +
                    "{\"key\":\"name\",\"value\":\"u_id\",\"describe\":\"名称\"}," +
                    "{\"key\":\"comment\",\"value\":\"ID\",\"describe\":\"字段描述\"}," +
                    "{\"key\":\"javaType\",\"value\":\"string\",\"describe\":\"java类型\"}," +
                    "{\"key\":\"dataType\",\"value\":\"varchar2(32)\",\"describe\":\"数据库类型\"}" +
                    "]" +
                    ",\"id2\":[" +
                    "{\"key\":\"name\",\"value\":\"name\",\"describe\":\"名称\"}," +
                    "{\"key\":\"comment\",\"value\":\"test\",\"describe\":\"字段描述\"}," +
                    "{\"key\":\"javaType\",\"value\":\"string\",\"describe\":\"java类型\"}," +
                    "{\"key\":\"dataType\",\"value\":\"varchar2(32)\",\"describe\":\"数据库类型\"}" +
                    "]" +
                    "}",
            "{" +
                    "\"id1\":[" +
                    "{\"key\":\"name\",\"value\":\"u_id\",\"describe\":\"名称\"}," +
                    "{\"key\":\"comment\",\"value\":\"ID\",\"describe\":\"字段描述\"}," +
                    "{\"key\":\"javaType\",\"value\":\"string\",\"describe\":\"java类型\"}," +
                    "{\"key\":\"dataType\",\"value\":\"varchar2(32)\",\"describe\":\"数据库类型\"}" +
                    "]" +
                    ",\"id2\":[" +
                    "{\"key\":\"name\",\"value\":\"name\",\"describe\":\"名称\"}," +
                    "{\"key\":\"comment\",\"value\":\"test\",\"describe\":\"字段描述\"}," +
                    "{\"key\":\"javaType\",\"value\":\"string\",\"describe\":\"java类型\"}," +
                    "{\"key\":\"dataType\",\"value\":\"varchar2(32)\",\"describe\":\"数据库类型\"}" +
                    "]" +
                    ",\"id3\":[" +
                    "{\"key\":\"name\",\"value\":\"sex\",\"describe\":\"名称\"}," +
                    "{\"key\":\"comment\",\"value\":\"test\",\"describe\":\"性别\"}," +
                    "{\"key\":\"javaType\",\"value\":\"string\",\"describe\":\"java类型\"}," +
                    "{\"key\":\"dataType\",\"value\":\"varchar2(32)\",\"describe\":\"数据库类型\"}" +
                    "]" +
                    "}"
    };

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
        sqlExecutor.exec(new CommonSql("drop table if exists s_history"));
        sqlExecutor.exec(new CommonSql("create table s_history\n" +
                "(\n" +
                "  u_id              varchar2(32) not null,\n" +
                "  type              varchar2(64) not null,\n" +
                "  describe          varchar2(512),\n" +
                "  primary_key_name  varchar2(32),\n" +
                "  primary_key_value varchar2(64),\n" +
                "  change_before     clob,\n" +
                "  change_after      clob,\n" +
                "  create_date       date not null,\n" +
                "  creator_id        varchar2(32)\n" +
                ")"));
        form = new Form();
        form.setName("test_form");
        form.setCreate_date(new Date());
        form.setHtml("<input field-id='id1'/><input field-id='id2'/>");
        form.setMeta(meta[0]);
        form.setU_id(RandomUtil.randomChar());
        formService.insert(form);
    }

    @Test
    public void testDeploy() throws Exception {
        //部署
        formService.deploy(form.getU_id());
        dataBase.getTable("test_form").createInsert()
                .insert(new InsertParam().value("name", "张三").value("u_id", "test"));
        Map<String, Object> data = dataBase.getTable("test_form")
                .createQuery().single(new QueryParam().where("name$LIKE", "张三"));
        Assert.assertEquals("张三", data.get("name"));
//        form.setMeta(meta[1]);
//        formService.update(form);
//        formService.deploy(form.getU_id());
    }

}