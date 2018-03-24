package org.hswebframework.web.boost;

import org.hswebframework.web.boost.bean.Converter;
import org.hswebframework.web.boost.bean.Copier;
import org.hswebframework.web.boost.bean.FastBeanCopier;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;

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
        source.setIds(new String[]{"1","2","3"});
        NestObject nestObject = new NestObject();
        nestObject.setAge(10);
        nestObject.setName("测试2");
        source.setNestObject(nestObject);
        Target target = new Target();
        Copier copier = FastBeanCopier.createCopier(Source.class, Target.class);
        FastBeanCopier.copy(source, target);
        long t = System.currentTimeMillis();
        Test2 test2 = new Test2();

        for (int i = 100_0000; i > 0; i--) {
            copier.copy(source,target,new HashSet<>(),FastBeanCopier.DEFAULT_CONVERT);
//            FastBeanCopier.copy(source, target);
        }
        System.out.println(System.currentTimeMillis() - t);

        System.out.println(target);
        System.out.println(target.getNestObject() == source.getNestObject());
    }


}