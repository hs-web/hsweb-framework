package org.hswebframework.web.bean.accessor;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hswebframework.web.dict.EnumDict;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.core.ResolvableType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class SmartTypeConverterTest {

    private SmartTypeConverter converter;

    @BeforeEach
    void setUp() {
        converter = new SmartTypeConverter();
    }

    @Test
    void testConvertNull() {
        // 测试null值转换
        assertNull(converter.convert(null, ResolvableType.forClass(String.class)));
        assertNull(converter.convert(null, ResolvableType.forClass(Integer.class)));
        assertNull(converter.convert(null, ResolvableType.forClass(Date.class)));
    }

    @Test
    void testConvertToObject() {
        // 测试转换为Object类型
        String source = "test";
        Object result = converter.convert(source, ResolvableType.forClass(Object.class));
        assertSame(source, result);
    }

    @Test
    void testConvertToString() {
        // 测试基本类型转换为String
        assertEquals("123", converter.convert(123, ResolvableType.forClass(String.class)));
        assertEquals("true", converter.convert(true, ResolvableType.forClass(String.class)));
        assertEquals("12.34", converter.convert(12.34, ResolvableType.forClass(String.class)));
        
        // 测试Date转换为String
        Date date = new Date();
        String result = (String) converter.convert(date, ResolvableType.forClass(String.class));
        assertNotNull(result);
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }

    @Test
    void testConvertToNumber() {
        // 测试字符串转换为数字
        assertEquals(123, converter.convert("123", ResolvableType.forClass(Integer.class)));
        assertEquals(123L, converter.convert("123", ResolvableType.forClass(Long.class)));
        assertEquals(12.34, converter.convert("12.34", ResolvableType.forClass(Double.class)));
        assertEquals(12.34f, converter.convert("12.34", ResolvableType.forClass(Float.class)));
        
        // 测试数字之间的转换
        assertEquals(123, converter.convert(123L, ResolvableType.forClass(Integer.class)));
        assertEquals(123.0, converter.convert(123, ResolvableType.forClass(Double.class)));
        
        // 测试Date转换为数字
        Date date = new Date();
        Long result = (Long) converter.convert(date, ResolvableType.forClass(Long.class));
        assertEquals(date.getTime(), result);
    }

    @Test
    void testConvertToDate() {
        // 测试数字转换为Date
        long timestamp = System.currentTimeMillis();
        Date result = (Date) converter.convert(timestamp, ResolvableType.forClass(Date.class));
        assertEquals(timestamp, result.getTime());
        
        // 测试Date复制
        Date originalDate = new Date();
        Date copiedDate = (Date) converter.convert(originalDate, ResolvableType.forClass(Date.class));
        assertEquals(originalDate.getTime(), copiedDate.getTime());
        assertNotSame(originalDate, copiedDate);
    }

    @Test
    void testConvertToEnum() {
        // 测试字符串转换为枚举
        TestEnum result1 = (TestEnum) converter.convert("ACTIVE", ResolvableType.forClass(TestEnum.class));
        assertEquals(TestEnum.ACTIVE, result1);
        
        // 测试枚举序号转换
        TestEnum result2 = (TestEnum) converter.convert("0", ResolvableType.forClass(TestEnum.class));
        assertEquals(TestEnum.ACTIVE, result2);
        
        // 测试大小写不敏感
        TestEnum result3 = (TestEnum) converter.convert("inactive", ResolvableType.forClass(TestEnum.class));
        assertEquals(TestEnum.INACTIVE, result3);
        
        // 测试无效值
        TestEnum result4 = (TestEnum) converter.convert("INVALID", ResolvableType.forClass(TestEnum.class));
        assertNull(result4);
    }

    @Test
    void testConvertToEnumDict() {
        // 测试EnumDict转换
        TestColor result1 = (TestColor) converter.convert("RED", ResolvableType.forClass(TestColor.class));
        assertEquals(TestColor.RED, result1);
        
        // 测试通过值转换
        TestColor result2 = (TestColor) converter.convert(1, ResolvableType.forClass(TestColor.class));
        assertEquals(TestColor.RED, result2);
        
        // 测试通过文本转换
        TestColor result3 = (TestColor) converter.convert("红色", ResolvableType.forClass(TestColor.class));
        assertEquals(TestColor.RED, result3);
    }

    @Test
    void testConvertToCollection() {
        // 测试数组转换为List
        String[] array = {"a", "b", "c"};
        List<?> result1 = (List<?>) converter.convert(array, ResolvableType.forClass(List.class));
        assertEquals(3, result1.size());
        assertEquals("a", result1.get(0));
        
        // 测试Collection转换为Set
        List<String> list = Arrays.asList("a", "b", "c", "a");
        Set<?> result2 = (Set<?>) converter.convert(list, ResolvableType.forClass(Set.class));
        assertEquals(3, result2.size());
        assertTrue(result2.contains("a"));
        
        // 测试字符串分割转换为List
        List<?> result3 = (List<?>) converter.convert("a,b,c", ResolvableType.forClass(List.class));
        assertEquals(3, result3.size());
        assertEquals("a", result3.get(0));
        
        // 测试Map的values转换为List
        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        List<?> result4 = (List<?>) converter.convert(map, ResolvableType.forClass(List.class));
        assertEquals(2, result4.size());
        assertTrue(result4.contains("value1"));
        assertTrue(result4.contains("value2"));
    }

    @Test
    void testConvertToCollectionWithGenerics() {
        // 测试带泛型的集合转换
        String[] array = {"123", "456", "789"};
        ResolvableType listType = ResolvableType.forClassWithGenerics(List.class, Integer.class);
        List<Integer> result = (List<Integer>) converter.convert(array, listType);
        
        assertEquals(3, result.size());
        assertEquals(123, result.get(0));
        assertEquals(456, result.get(1));
        assertEquals(789, result.get(2));
    }

    @Test
    void testConvertToArray() {
        // 测试List转换为数组
        List<String> list = Arrays.asList("a", "b", "c");
        String[] result1 = (String[]) converter.convert(list, ResolvableType.forClass(String[].class));
        assertEquals(3, result1.length);
        assertEquals("a", result1[0]);
        
        // 测试数字List转换为int数组
        List<Integer> intList = Arrays.asList(1, 2, 3);
        int[] result2 = (int[]) converter.convert(intList, ResolvableType.forClass(int[].class));
        assertEquals(3, result2.length);
        assertEquals(1, result2[0]);
    }

    @Test
    void testConvertToMap() {
        // 测试Map复制
        Map<String, Object> sourceMap = new HashMap<>();
        sourceMap.put("key1", "value1");
        sourceMap.put("key2", 123);
        
        Map<?, ?> result1 = (Map<?, ?>) converter.convert(sourceMap, ResolvableType.forClass(Map.class));
        assertEquals(2, result1.size());
        assertEquals("value1", result1.get("key1"));
        assertEquals(123, result1.get("key2"));
        
        // 测试不同类型的Map
        Map<?, ?> result2 = (Map<?, ?>) converter.convert(sourceMap, ResolvableType.forClass(LinkedHashMap.class));
        assertEquals(2, result2.size());
        assertTrue(result2 instanceof LinkedHashMap);
        
        // 测试Collection转换为Map
        List<String> list = Arrays.asList("a", "b", "c");
        Map<?, ?> result3 = (Map<?, ?>) converter.convert(list, ResolvableType.forClass(Map.class));
        assertEquals(3, result3.size());
        assertEquals("a", result3.get(0));
        assertEquals("b", result3.get(1));
        assertEquals("c", result3.get(2));
    }

    @Test
    void testConvertToMapWithGenerics() {
        // 测试带泛型的Map转换
        List<String> list = Arrays.asList("123", "456", "789");
        ResolvableType mapType = ResolvableType.forClassWithGenerics(Map.class, Integer.class, Integer.class);
        Map<Integer, Integer> result = (Map<Integer, Integer>) converter.convert(list, mapType);
        
        assertEquals(3, result.size());
        assertEquals(123, result.get(0));
        assertEquals(456, result.get(1));
        assertEquals(789, result.get(2));
    }

    @Test
    void testConvertToBean() {
        // 测试Map转换为Bean
        Map<String, Object> map = new HashMap<>();
        map.put("name", "测试用户");
        map.put("age", 25);
        map.put("active", true);
        
        TestBean result = (TestBean) converter.convert(map, ResolvableType.forClass(TestBean.class));
        assertNotNull(result);
        assertEquals("测试用户", result.getName());
        assertEquals(25, result.getAge());
        assertTrue(result.isActive());
        
        // 测试Bean转换为Bean
        TestBean source = new TestBean();
        source.setName("源Bean");
        source.setAge(30);
        source.setActive(false);
        
        TestBean result2 = (TestBean) converter.convert(source, ResolvableType.forClass(TestBean.class));
        assertNotNull(result2);
        assertEquals("源Bean", result2.getName());
        assertEquals(30, result2.getAge());
        assertFalse(result2.isActive());
        assertNotSame(source, result2);
    }

    @Test
    void testConvertComplexScenarios() {
        // 测试复杂的转换场景
        Map<String, Object> complexMap = new HashMap<>();
        complexMap.put("name", "复杂对象");
        complexMap.put("numbers", Arrays.asList("1", "2", "3"));
        complexMap.put("colors", Arrays.asList("RED", "BLUE"));
        complexMap.put("metadata", Map.of("key1", "value1", "key2", "value2"));
        
        ComplexBean result = (ComplexBean) converter.convert(complexMap, ResolvableType.forClass(ComplexBean.class));
        assertNotNull(result);
        assertEquals("复杂对象", result.getName());
        
        // 验证数字列表转换
        assertNotNull(result.getNumbers());
        assertEquals(3, result.getNumbers().size());
        
        // 验证枚举列表转换
        assertNotNull(result.getColors());
        assertEquals(2, result.getColors().size());
        
        // 验证嵌套Map
        assertNotNull(result.getMetadata());
        assertEquals(2, result.getMetadata().size());
    }

    @Test
    void testPerformance() {
        // 简单的性能测试
        int iterations = 100_0000;
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < iterations; i++) {
            converter.convert("123", ResolvableType.forClass(Integer.class));
            converter.convert(Arrays.asList("a", "b", "c"), ResolvableType.forClass(Set.class));
//            converter.convert(new HashMap<>(), ResolvableType.forClass(TestBean.class));
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("SmartTypeConverter性能测试：" + iterations + "次转换耗时：" + duration + "ms");
        assertTrue(duration < 5000, "转换性能应该在合理范围内");
    }

    // 测试用枚举
    public enum TestEnum {
        ACTIVE, INACTIVE
    }

    // 测试用EnumDict
    @Getter
    @AllArgsConstructor
    public enum TestColor implements EnumDict<Integer> {
        RED(1, "红色"),
        BLUE(2, "蓝色");

        private Integer value;
        private String text;
    }

    // 测试用Bean
    @Data
    @NoArgsConstructor
    public static class TestBean {
        private String name;
        private int age;
        private boolean active;
    }

    // 复杂测试Bean
    @Data
    @NoArgsConstructor
    public static class ComplexBean {
        private String name;
        private List<Integer> numbers;
        private List<TestColor> colors;
        private Map<String, String> metadata;
    }
} 