package org.hswebframework.web.boost;

import org.apache.commons.beanutils.BeanUtils;
import org.hswebframework.web.boost.bean.Converter;
import org.hswebframework.web.boost.bean.Copier;
import org.hswebframework.web.boost.bean.FastBeanCopier;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 * @since
 */
public class FastBeanCopierTest {

    @Test
    public void test() {
        Source source = new Source();
        source.setAge(100);
        source.setName("测试");
        source.setIds(new String[]{"1", "2", "3"});
        NestObject nestObject = new NestObject();
        nestObject.setAge(10);
        nestObject.setName("测试2");
        source.setNestObject(nestObject);


        Target target = new Target();
        FastBeanCopier.copy(source, target);

        long t = System.currentTimeMillis();
        for (int i = 100_0000; i > 0; i--) {
            FastBeanCopier.copy(source, target);
        }
        System.out.println(System.currentTimeMillis() - t);

        System.out.println(target);
        System.out.println(target.getNestObject() == source.getNestObject());
//        Source source1=new Source();

//        FastBeanCopier.copy(source,source1);

//        System.out.println(source1);
//
//        t = System.currentTimeMillis();
//
//        for (int i = 100_0000; i > 0; i--) {
//            try {
//                BeanUtils.copyProperties(source, target);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        System.out.println(System.currentTimeMillis() - t);
//        System.out.println(target);
//        System.out.println(target.getNestObject() == source.getNestObject());
    }


    @Test
    public void testCopyToMap() {
        Source source = new Source();
        source.setAge(100);
        source.setName("测试");
        source.setIds(new String[]{"1", "2", "3"});
        NestObject nestObject = new NestObject();
        nestObject.setAge(10);
        nestObject.setName("测试2");
        source.setNestObject(nestObject);
        Map<String,Object> target = new HashMap<>();
        FastBeanCopier.copy(source, target);

        System.out.println(target);
    }


}