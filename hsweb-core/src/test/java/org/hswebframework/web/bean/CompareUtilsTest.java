package org.hswebframework.web.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.utils.time.DateFormatter;
import org.hswebframework.web.dict.EnumDict;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.Assert.*;

public class CompareUtilsTest {

    @Test
    public void nullTest() {

        Assert.assertFalse(CompareUtils.compare(1, null));

        Assert.assertFalse(CompareUtils.compare((Object) null, 1));
        Assert.assertTrue(CompareUtils.compare((Object) null, null));
        Assert.assertFalse(CompareUtils.compare((Number) null, 1));
        Assert.assertTrue(CompareUtils.compare((Number) null, null));
        Assert.assertFalse(CompareUtils.compare((Date) null, 1));
        Assert.assertTrue(CompareUtils.compare((Date) null, null));
        Assert.assertFalse(CompareUtils.compare((String) null, 1));
        Assert.assertTrue(CompareUtils.compare((String) null, null));
        Assert.assertFalse(CompareUtils.compare((Collection) null, 1));
        Assert.assertTrue(CompareUtils.compare((Collection) null, null));
        Assert.assertFalse(CompareUtils.compare((Map<?, ?>) null, 1));
        Assert.assertTrue(CompareUtils.compare((Map<?, ?>) null, null));

    }

    @Test
    public void numberTest() {
        Assert.assertTrue(CompareUtils.compare(1, 1));
        Assert.assertTrue(CompareUtils.compare(1, 1D));
        Assert.assertTrue(CompareUtils.compare(1, 1.0D));
        Assert.assertTrue(CompareUtils.compare(1e3,"1e3"));
        Assert.assertTrue(CompareUtils.compare(1e3,"1000"));

        Assert.assertTrue(CompareUtils.compare(1, "1"));
        Assert.assertTrue(CompareUtils.compare("1.0", 1));
        Assert.assertFalse(CompareUtils.compare(1, "1a"));
    }

    @Test
    public void enumTest() {

        Assert.assertTrue(CompareUtils.compare(TestEnum.BLUE, "blue"));

        Assert.assertFalse(CompareUtils.compare(TestEnum.RED, "blue"));


        Assert.assertTrue(CompareUtils.compare(TestEnumDic.BLUE, "blue"));

        Assert.assertFalse(CompareUtils.compare(TestEnumDic.RED, "blue"));


        Assert.assertTrue(CompareUtils.compare(TestEnumDic.BLUE, "蓝色"));

        Assert.assertFalse(CompareUtils.compare(TestEnumDic.RED, "蓝色"));


    }

    @Test
    public void stringTest() {

        Assert.assertTrue(CompareUtils.compare("20180101", DateFormatter.fromString("20180101")));

        Assert.assertTrue(CompareUtils.compare(1,"1"));

        Assert.assertTrue(CompareUtils.compare("1", 1));

        Assert.assertTrue(CompareUtils.compare("1.0", 1.0D));

        Assert.assertTrue(CompareUtils.compare("1.01", 1.01D));

        Assert.assertTrue(CompareUtils.compare("1,2,3", Arrays.asList(1, 2, 3)));

        Assert.assertTrue(CompareUtils.compare("blue", TestEnumDic.BLUE));

        Assert.assertTrue(CompareUtils.compare("BLUE", TestEnum.BLUE));


    }

    @Test
    public void dateTest() {

        Date date = new Date();

        Assert.assertTrue(CompareUtils.compare(date, new Date(date.getTime())));
        Assert.assertTrue(CompareUtils.compare(date, DateFormatter.toString(date, "yyyy-MM-dd")));
        Assert.assertTrue(CompareUtils.compare(date, DateFormatter.toString(date, "yyyy-MM-dd HH:mm:ss")));


        Assert.assertTrue(CompareUtils.compare(date, date.getTime()));
        Assert.assertTrue(CompareUtils.compare(date.getTime(), date));

    }

    @Test
    public void connectionTest() {
        Date date = new Date();

        Assert.assertTrue(CompareUtils.compare(100, new BigDecimal("100")));

        Assert.assertTrue(CompareUtils.compare(new BigDecimal("100"), 100.0D));

        Assert.assertTrue(CompareUtils.compare(Arrays.asList(1, 2, 3), Arrays.asList("3", "2", "1")));

        Assert.assertFalse(CompareUtils.compare(Arrays.asList(1, 2, 3), Arrays.asList("3", "3", "1")));

        Assert.assertFalse(CompareUtils.compare(Arrays.asList(1, 2, 3), Arrays.asList("3", "1")));

        Assert.assertFalse(CompareUtils.compare(Arrays.asList(1, 2, 3), Collections.emptyList()));
        Assert.assertFalse(CompareUtils.compare(Collections.emptyList(), Arrays.asList(1, 2, 3)));

        Assert.assertTrue(CompareUtils.compare(Arrays.asList(date, 3), Arrays.asList("3", DateFormatter.toString(date, "yyyy-MM-dd"))));

    }

    @Test
    public void mapTest() {
        Date date = new Date();

        Assert.assertTrue(CompareUtils.compare(Collections.singletonMap("test", "123"), Collections.singletonMap("test", 123)));


        Assert.assertFalse(CompareUtils.compare(Collections.singletonMap("test", "123"), Collections.emptyMap()));

        Assert.assertTrue(CompareUtils.compare(Collections.singletonMap("test", "123"), new TestBean("123")));

        Assert.assertTrue(CompareUtils.compare(Collections.singletonMap("test", date), new TestBean(DateFormatter.toString(date, "yyyy-MM-dd"))));

    }

    @Test
    public void beanTest() {
        Date date = new Date();

        Assert.assertTrue(CompareUtils.compare(new TestBean(date), new TestBean(DateFormatter.toString(date, "yyyy-MM-dd"))));

        Assert.assertTrue(CompareUtils.compare(new TestBean(1), new TestBean("1")));

        Assert.assertTrue(CompareUtils.compare(new TestBean(1), new TestBean("1.0")));
        Assert.assertFalse(CompareUtils.compare(new TestBean(1), new TestBean("1.0000000001")));

    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class TestBean {
        private Object test;
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