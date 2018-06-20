package org.hswebframework.web.excel;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hswebframework.web.commons.bean.Bean;
import org.hswebframework.web.commons.entity.DataStatusEnum;
import org.junit.Assert;
import org.junit.Test;


/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
public class DefaultExcelImporterTest {


    @Test
    public void test() {
        ExcelImporter.Result<TestBean> result = ExcelImporter
                .instance
                .doImport(this.getClass().getResourceAsStream("/test.xls"), TestBean.class, bean -> null);

        Assert.assertEquals(result.success, 1);
        System.out.println(JSON.toJSONString(result.getData(), SerializerFeature.PrettyFormat));
        TestBean bean = result.getData().get(0);
        Assert.assertNotNull(bean.status);
        Assert.assertNotNull(bean.nest);
        Assert.assertNotNull(bean.nest.nest);

    }

    @Getter
    @Setter
    @ToString
    public static class TestBean implements Bean {

        private static final long serialVersionUID = -5394537136669692305L;

        @Excel("姓名")
        private String name;

        @Excel("年龄")
        private int age;

        @Excel("状态")
        private DataStatusEnum status;

        @Excel("嵌套-")
        private TestBean nest;
    }


}