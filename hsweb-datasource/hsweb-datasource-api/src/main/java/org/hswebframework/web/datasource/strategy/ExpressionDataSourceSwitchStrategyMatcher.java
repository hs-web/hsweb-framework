package org.hswebframework.web.datasource.strategy;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.AntPathMatcher;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 表达式方式切换数据源,在配置文件中设置:
 * <pre>
 *    hsweb:
 *      datasource:
 *          switcher:
 *              test: # 只是一个标识
 *              # 拦截类和方法的表达式
 *              expression: org.hswebframework.**.*Service.find*
 *              # 使用数据源
 *              data-source-id: read_db
 * </pre>
 *
 * @author zhouhao
 * @since 3.0.0-RC
 */
public class ExpressionDataSourceSwitchStrategyMatcher extends CachedDataSourceSwitchStrategyMatcher {

    @Getter
    @Setter
    private Map<String, ExpressionStrategy> switcher = new HashMap<>();

    private static AntPathMatcher antPathMatcher = new AntPathMatcher(".");

    @Override
    public Strategy createStrategyIfMatch(Class target, Method method) {
        if (switcher.isEmpty()) {
            return null;
        }
        String text = target.getName().concat(".").concat(method.getName());

        return switcher.entrySet().stream()
                .filter(entry -> antPathMatcher.match(entry.getValue().getExpression(), text))
                .peek(entry -> entry.getValue().setId(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    @Getter
    @Setter
    public static class ExpressionStrategy implements Strategy {
        private boolean useDefaultDataSource = false;
        private boolean fallbackDefault      = false;
        private String  dataSourceId         = null;
        private String expression;
        private String id;

        public boolean isUseDefaultDataSource() {
            return useDefaultDataSource && dataSourceId == null;
        }

        @Override
        public String toString() {
            return "Expression Strategy(use(" + (isUseDefaultDataSource() ? "default" : getDataSourceId()) + "),expression:" + getExpression() + ")";
        }
    }
}
