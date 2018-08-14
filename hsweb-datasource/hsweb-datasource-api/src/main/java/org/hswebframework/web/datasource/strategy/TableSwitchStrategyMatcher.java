package org.hswebframework.web.datasource.strategy;

import org.hswebframework.web.boost.aop.context.MethodInterceptorContext;
import org.hswebframework.web.datasource.DynamicDataSource;
import org.hswebframework.web.datasource.exception.DataSourceNotFoundException;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 数据库表切换策略,可通过此接口来自定义表切换的方式
 *
 * @author zhouhao
 * @since 3.0.0-RC
 */
public interface TableSwitchStrategyMatcher {

    /**
     * 匹配类和方法,返回是否需要进行表切换
     *
     * @param target 类
     * @param method 方法
     * @return 是否需要进行数据源切换
     */
    boolean match(Class target, Method method);

    /**
     * 获取表切换策略
     *
     * @param context aop上下文
     * @return 切换策略
     */
    Strategy getStrategy(MethodInterceptorContext context);

    /**
     * 表切换策略
     */
    interface Strategy {
        /**
         * @return 表映射关系
         * @see org.hswebframework.web.datasource.switcher.TableSwitcher#getTable(String)
         */
        Map<String, String> getMapping();

        static Strategy of(Map<String, String> mapping) {
            return () -> mapping;
        }

        static Strategy of(Supplier<Map<String, String>> supplier) {
            return of(supplier.get());
        }

        static Strategy single(String source, String target) {
            return of(() -> Collections.singletonMap(source, target));
        }
    }

}
