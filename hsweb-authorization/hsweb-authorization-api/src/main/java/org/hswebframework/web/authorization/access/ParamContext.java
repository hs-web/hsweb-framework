package org.hswebframework.web.authorization.access;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

/**
 * 参数上下文，用于获取当前进行操作的方法的各种参数信息，如:当前所在类实例，参数集合，注解
 *
 * @author zhouhao
 * @see 3.0
 */
public interface ParamContext extends Serializable {

    /**
     * 获取当前类实例
     *
     * @return 类实例对象
     */
    Object getTarget();

    /**
     * 当前操作的方法
     *
     * @return 方法实例
     */
    Method getMethod();

    /**
     * 根据参数名获取参数值,此参数为方法的参数,而非http参数 <br>
     * 如：当前被操作的方法为 query(QueryParam param); 调用getParameter("param"); 则返回QueryParam实例<br>
     * 注意:返回值为Optional对象,使用方法见{@link Optional}<br>
     *
     * @param name 参数名称
     * @param <T>  参数泛型
     * @return Optional
     */
    <T> Optional<T> getParameter(String name);

    /**
     * 获取当前操作方法或实例上指定类型的泛型,如果方法上未获取到,则获取实例类上的注解。实例类上未获取到,则返回null
     *
     * @param type 注解的类型
     * @param <T>  注解泛型
     * @return 注解
     */
    <T extends Annotation> T getAnnotation(Class<T> type);

    /**
     * 获取全部参数
     *
     * @return 参数集合
     * @see this#getParameter(String)
     */
    Map<String, Object> getParams();
}
