package org.hswebframework.web.service.form.simple;

import org.hsweb.ezorm.rdb.executor.SqlExecutor;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.entity.form.DynamicFormColumnEntity;
import org.hswebframework.web.entity.form.DynamicFormEntity;
import org.hswebframework.web.service.form.DynamicFormColumnService;
import org.hswebframework.web.service.form.DynamicFormOperationService;
import org.hswebframework.web.service.form.DynamicFormService;
import org.hswebframework.web.tests.SimpleWebApplicationTests;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.JDBCType;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimpleDynamicFormServiceTest extends SimpleWebApplicationTests {

    @Autowired
    private DynamicFormService dynamicFormService;

    @Autowired
    private DynamicFormColumnService dynamicFormColumnService;

    @Autowired
    private DynamicFormOperationService dynamicFormOperationService;
    @Autowired
    private SqlExecutor sqlExecutor;

    @Test
    public void testDeploy() throws SQLException {
        DynamicFormEntity form = entityFactory.newInstance(DynamicFormEntity.class);
        form.setName("test");
        form.setDatabaseTableName("f_test");
        String id = dynamicFormService.insert(form);
        DynamicFormColumnEntity column_id = entityFactory.newInstance(DynamicFormColumnEntity.class);
        column_id.setFormId(id);
        column_id.setName("id");
        column_id.setDescribe("ID");
        column_id.setJavaType("string");
        column_id.setJdbcType(JDBCType.VARCHAR.getName());
        column_id.setLength(32);
        DynamicFormColumnEntity column_name = entityFactory.newInstance(DynamicFormColumnEntity.class);
        column_name.setFormId(id);
        column_name.setName("name");
        column_name.setDescribe("姓名");
        column_name.setJavaType("string");
        column_name.setJdbcType(JDBCType.VARCHAR.getName());
        column_name.setLength(32);

        DynamicFormColumnEntity column_age = entityFactory.newInstance(DynamicFormColumnEntity.class);
        column_age.setFormId(id);
        column_age.setName("age");
        column_age.setDescribe("年龄");
        column_age.setJavaType("int");
        column_age.setJdbcType(JDBCType.NUMERIC.getName());
        column_age.setPrecision(4);
        column_age.setScale(0);

        Stream.of(column_id,column_name,column_age).forEach(dynamicFormColumnService::insert);
        dynamicFormService.deploy(id);

        dynamicFormOperationService.insert(form.getId(),new HashMap<String,Object>(){
            {
                put("id","test");
                put("name","张三");
                put("age",10);
            }
        });
       List<Object> objects= dynamicFormOperationService.select(form.getId(),new QueryParamEntity());

        Assert.assertTrue(objects.size()==1);
        System.out.println(objects);
        sqlExecutor.list("select * from f_test");
    }
}