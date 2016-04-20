package org.hsweb.web.bean.common;

import org.webbuilder.utils.common.StringUtils;

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
        this.term.put(key, changeTermValue(key, value));
        return (R) this;
    }

    public R where(Map<String, Object> conditions) {
        changeTerm(conditions);
        this.term.putAll(conditions);
        return (R) this;
    }

    public Map<String, Object> getTerm() {
        changeTerm(this.term);
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

    public R setTerm(Map<String, Object> term) {
        this.term = term;
        return (R) this;
    }

    protected void changeTerm(Map<String, Object> term) {
        if (term != null) {
            term.entrySet().forEach((e) -> e.setValue(changeTermValue(e.getKey(), e.getValue())));
        }
    }

    public Object changeTermValue(String key, Object value) {
        //将IN条件的值转换为Iterable
        if (key.endsWith("$IN")) {
            if (value == null) return new ArrayList<>();
            if (!(value instanceof Iterable)) {
                if (value instanceof String) {
                    String[] arr = ((String) value).split("[, ;]");
                    Object[] objArr = new Object[arr.length];
                    for (int i = 0; i < arr.length; i++) {
                        String str = arr[i];
                        Object val = str;
                        if (StringUtils.isInt(str))
                            val = StringUtils.toInt(str);
                        else if (StringUtils.isDouble(str))
                            val = StringUtils.toDouble(str);
                        objArr[i] = val;
                    }
                    return Arrays.asList(objArr);
                } else if (value.getClass().isArray()) {
                    return Arrays.asList(((Object[]) value));
                } else {
                    return Arrays.asList(value);
                }
            }
        }
        return value;
    }

}
