package org.hswebframework.web.boost;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 * @since
 */
public class CompilerTest {

    @Test
    public void Test() throws Exception {
        Compiler<TestApi> compiler = Compiler.create(TestApi.class)
                .addMethod("public void hello(int i){System.out.println(\"aa\"+i);}");
        Class<TestApi> czz = compiler.getTargetClass();

        System.out.println(czz.getConstructors().length);
        compiler.newInstance().hello(11);
    }

    public interface TestApi {
         void hello(int i);
    }
}