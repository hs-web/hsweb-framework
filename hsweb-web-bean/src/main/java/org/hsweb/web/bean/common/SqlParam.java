package org.hsweb.web.bean.common;

import java.util.*;

/**
 * Created by zhouhao on 16-4-19.
 */
public class SqlParam<R extends SqlParam> {
    /**
     * 执行条件
     */
    protected Map<String, Object> term = new HashMap<>();

    /**
     * 指定要处理的字段
     */
    protected Set<String> includes = new LinkedHashSet<>();

    /**
     * 指定不处理的字段
     */
    protected Set<String> excludes = new LinkedHashSet<>();

    public R includes(String... fields) {
        includes.addAll(Arrays.asList(fields));
        return (R) this;
    }

    public R excludes(String... fields) {
        excludes.addAll(Arrays.asList(fields));
        includes.removeAll(Arrays.asList(fields));
        return (R) this;
    }

    public R where(String key, Object value) {
        this.term.put(key, value);
        return (R) this;
    }

    public R where(Map<String, Object> conditions) {
        this.term.putAll(conditions);
        return (R) this;
    }

    public Map<String, Object> getTerm() {
        return term;
    }

    public Set<String> getIncludes() {
        return includes;
    }

    public Set<String> getExcludes() {
        return excludes;
    }

    public void setIncludes(Set<String> includes) {
        this.includes = includes;
    }

    public void setExcludes(Set<String> excludes) {
        this.excludes = excludes;
    }

    public void setTerm(Map<String, Object> term) {
        this.term = term;
    }
}
