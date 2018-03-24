import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtNewMethod;
import org.junit.Test;


/**
 * TODO 完成注释
 *
 * @author zhouhao
 * @since
 */
public class JaninoTest {

    @Test
    public void Test() throws Exception {
        ClassPool classPool = new ClassPool(true);
        classPool.insertClassPath(new ClassClassPath(this.getClass()));

        CtClass ctClass = classPool.makeClass("Test1");
        ctClass.setInterfaces(new CtClass[]{classPool.get(TestApi.class.getName())});

        ctClass.addMethod(CtNewMethod.make("public void hello(int i){System.out.println(i);}", ctClass));

        TestApi api = (TestApi) ctClass.toClass().newInstance();
        api.hello(1);
    }

    interface TestApi {
        void hello(int i);
    }
}
