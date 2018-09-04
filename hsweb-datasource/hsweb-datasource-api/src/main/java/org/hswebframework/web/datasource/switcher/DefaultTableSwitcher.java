package org.hswebframework.web.datasource.switcher;

import org.hswebframework.web.ThreadLocalUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
public class DefaultTableSwitcher implements TableSwitcher {

    private Map<String, String> staticMapping = new HashMap<>();

    @Override
    public void use(String source, String target) {
        getMapping().put(source, target);
    }

    private Map<String, String> getMapping() {
        return ThreadLocalUtils.get(DefaultTableSwitcher.class.getName() + "_current", HashMap::new);
    }

    @Override
    public String getTable(String name) {
        return getMapping()
                .getOrDefault(name, staticMapping.getOrDefault(name, name));
    }

    @Override
    public void reset() {
        ThreadLocalUtils.remove(DefaultTableSwitcher.class.getName() + "_current");
    }
}
