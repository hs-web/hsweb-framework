package org.hsweb.web.bean.common;

import com.alibaba.fastjson.JSON;
import org.webbuilder.utils.common.MapUtils;
import org.webbuilder.utils.common.StringUtils;

import java.util.*;

/**
 * Created by zhouhao on 16-4-19.
 */
public class SqlParam<R extends SqlParam> {

    @Deprecated
    protected Map<String, Object> term = new HashMap<>();

    /**
     * 条件
     */
    protected List<Term> terms = new LinkedList<>();

    /**
     * 指定要处理的字段
     */
    protected Set<String> includes = new LinkedHashSet<>();

    /**
     * 指定不处理的字段
     */
    protected Set<String> excludes = new LinkedHashSet<>();

    public R or(String termString, Object value) {
        Term term = new Term();
        term.setField(termString);
        term.setValue(value);
        term.setType(Term.Type.or);
        terms.add(term);
        return (R) this;
    }

    public R and(String termString, Object value) {
        Term term = new Term();
        term.setField(termString);
        term.setValue(value);
        term.setType(Term.Type.and);
        terms.add(term);
        return (R) this;
    }

    public Term nest() {
        return nest(null, null);
    }

    public Term orNest() {
        return orNest(null, null);
    }

    public Term nest(String termString, Object value) {
        Term term = new Term();
        term.setField(termString);
        term.setValue(value);
        term.setType(Term.Type.and);
        term.setNest(true);
        terms.add(term);
        return term;
    }

    public Term orNest(String termString, Object value) {
        Term term = new Term();
        term.setField(termString);
        term.setValue(value);
        term.setType(Term.Type.or);
        term.setNest(true);
        terms.add(term);
        return term;
    }


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
        and(key, value);
        return (R) this;
    }

    public R where(Map<String, Object> conditions) {
        initTermByMap(conditions);
        return (R) this;
    }

    public Set<String> getIncludes() {
        if (includes == null) includes = new LinkedHashSet<>();
        return includes;
    }

    public Set<String> getExcludes() {
        if (excludes == null) excludes = new LinkedHashSet<>();
        return excludes;
    }

    public void setIncludes(Set<String> includes) {
        this.includes = includes;
    }

    public void setExcludes(Set<String> excludes) {
        this.excludes = excludes;
    }

    public List<Term> getTerms() {
        return terms;
    }

    public void setTerms(List<Term> terms) {
        this.terms = terms;
    }

    @Deprecated
    public Map<String, Object> getTerm() {
        if (term.isEmpty() && !terms.isEmpty()) {
            terms.forEach(term1 -> {
                if (!StringUtils.isNullOrEmpty(term1.getField()))
                    term.put(term1.getField(), term1.getValue());
            });
        }
        return term;
    }

    @Deprecated
    public void setTerm(Map<String, Object> term) {
        this.term = term;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    protected void initTermByMap(Map<String, Object> param) {
        param.forEach((k, v) -> {
            String field = String.valueOf(param.get("field"));
            Object value = param.get("value");
            if (StringUtils.isNullOrEmpty(field) || StringUtils.isNullOrEmpty(value)) return;
            String type = String.valueOf(param.get("type"));
            String queryType = String.valueOf(param.get("queryType"));
            Term nest = new Term();
            nest.setField(field);
            nest.setValue(value);
            nest.setType(Term.Type.valueOf(type));
            nest.setTermType(TermType.valueOf(queryType));
            terms.add(nest);
        });
    }

    public static  SqlParam build() {
        return new SqlParam<>();
    }
}
