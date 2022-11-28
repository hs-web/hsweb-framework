package org.hswebframework.web.bean;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.Setter;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author zhouhao
 * @since 3.0
 */
public class FastBeanCopierTest {

    @Test
    public void test() throws InvocationTargetException, IllegalAccessException {
        Source source = new Source();
        source.setAge(100);
        source.setName("测试");
        source.setIds(new String[]{"1", "2", "3"});
        source.setAge2(2);
        source.setBoy2(true);
        source.setColor(Color.RED);
        source.setNestObject2(Collections.singletonMap("name", "mapTest"));
        NestObject nestObject = new NestObject();
        nestObject.setAge(10);
        nestObject.setPassword("1234567");
        nestObject.setName("测试2");
        source.setNestObject(nestObject);
        source.setNestObject3(nestObject);

        Target target = new Target();
        FastBeanCopier.copy(source, target);


        System.out.println(source);
        System.out.println(target);
        System.out.println(target.getNestObject() == source.getNestObject());
    }

    @Test
    public void testMapArray() {
        Map<String, Object> data = new HashMap<>();
        data.put("colors", Arrays.asList("RED"));


        Target target = new Target();
        FastBeanCopier.copy(data, target);


        System.out.println(target);
        Assert.assertNotNull(target.getColors());
        Assert.assertSame(target.getColors()[0], Color.RED);

    }

    @Test
    public void testMapList() {
        Map<String, Object> data = new HashMap<>();
        data.put("templates",   new HashMap() {
            {
                put("0", Collections.singletonMap("name", "test"));
                put("1", Collections.singletonMap("name", "test"));
            }
        });

        Config config = FastBeanCopier.copy(data, new Config());

        Assert.assertNotNull(config);
        Assert.assertNotNull(config.templates);
        System.out.println(config.templates);
        Assert.assertEquals(2,config.templates.size());


    }

    @Getter
    @Setter
    public static class Config {
        private List<Template> templates;
    }

    @Getter
    @Setter
    public static class Template {
        private String name;

        @Override
        public String toString() {
            return "name:"+name;
        }
    }

    @Test
    public void testCopyMap() {


        Source source = new Source();
        source.setAge(100);
        source.setName("测试");
//        source.setIds(new String[]{"1", "2", "3"});
        NestObject nestObject = new NestObject();
        nestObject.setAge(10);
        nestObject.setName("测试2");
//        source.setNestObject(nestObject);


        Map<String, Object> target = new HashMap<>();


        System.out.println(FastBeanCopier.copy(source, target, FastBeanCopier.include("age")));

        System.out.println(target);
        System.out.println(FastBeanCopier.copy(target, new Target()));
    }


    @Test
    public void testProxy() {
        AtomicReference<Object> reference = new AtomicReference<>();

        ProxyTest test = (ProxyTest) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
                                                            new Class[]{ProxyTest.class}, (proxy, method, args) -> {
                    if (method.getName().equals("getName")) {
                        return "test";
                    }

                    if (method.getName().equals("setName")) {
                        reference.set(args[0]);
                        return null;
                    }

                    return null;
                });

        Target source = new Target();

        FastBeanCopier.copy(test, source);
        Assert.assertEquals(source.getName(), test.getName());


        source.setName("test2");
        FastBeanCopier.copy(source, test);

        Assert.assertEquals(reference.get(), source.getName());
    }

    @Test
    public void testGetProperty() {

        Assert.assertEquals(1, FastBeanCopier.getProperty(ImmutableMap.of("a", 1, "b", 2), "a"));

    }


    public interface ProxyTest {
        String getName();

        void setName(String name);
    }

}