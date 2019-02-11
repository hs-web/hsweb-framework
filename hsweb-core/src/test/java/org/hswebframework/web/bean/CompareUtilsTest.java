package org.hswebframework.web.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.utils.time.DateFormatter;
import org.hswebframework.web.dict.EnumDict;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.Assert.*;

public class CompareUtilsTest {


    @Test
    public void compareTest() {
        Assert.assertTrue(CompareUtils.compare(1, "1"));
        Assert.assertTrue(CompareUtils.compare("1", 1));

        Assert.assertFalse(CompareUtils.compare(1, "1a"));

        Assert.assertTrue(CompareUtils.compare(TestEnum.BLUE, "blue"));

        Assert.assertFalse(CompareUtils.compare(TestEnum.RED, "blue"));


        Assert.assertTrue(CompareUtils.compare(TestEnumDic.BLUE, "blue"));

        Assert.assertFalse(CompareUtils.compare(TestEnumDic.RED, "blue"));


        Assert.assertTrue(CompareUtils.compare(TestEnumDic.BLUE, "蓝色"));

        Assert.assertFalse(CompareUtils.compare(TestEnumDic.RED, "蓝色"));


        Assert.assertTrue(CompareUtils.compare(DateFormatter.fromString("20180101"), "20180101"));

        Date date = new Date();

        Assert.assertTrue(CompareUtils.compare(date, new Date(date.getTime())));
        Assert.assertTrue(CompareUtils.compare(date, DateFormatter.toString(date, "yyyy-MM-dd")));
        Assert.assertTrue(CompareUtils.compare(date, DateFormatter.toString(date, "yyyy-MM-dd HH:mm:ss")));


        Assert.assertTrue(CompareUtils.compare(date, date.getTime()));
        Assert.assertTrue(CompareUtils.compare(date.getTime(), date));


        Assert.assertTrue(CompareUtils.compare(100,new BigDecimal("100")));

        Assert.assertTrue(CompareUtils.compare(new BigDecimal("100"),100.0D));


    }


    enum TestEnum {
        RED, BLUE
    }

    @Getter
    @AllArgsConstructor
    enum TestEnumDic implements EnumDict<String> {
        RED("RED", "红色"), BLUE("BLUE", "蓝色");

        private String value;
        private String text;

    }

}