package org.hsweb.web.bean.common;


import org.hsweb.ezorm.core.param.Term;
import org.hsweb.ezorm.core.param.TermType;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryParam extends org.hsweb.ezorm.core.param.QueryParam implements Serializable {
    private static final long                serialVersionUID = 7941767360194797891L;
    private              Map<String, Object> param            = new HashMap<>();

    public QueryParam noPaging() {
        setPaging(false);
        return this;
    }

    @Override
    public void setTerms(List<Term> terms) {
        super.setTerms(terms);
    }

    @Override
    public List<Term> getTerms() {
        checkTerm(terms);
        return super.getTerms();
    }

    protected void checkTerm(List<Term> terms) {
        terms.forEach(term -> {
            if (term.getTermType().equals(TermType.func)) {
                term.setTermType(TermType.eq);
            }
            checkTerm(term.getTerms());
        });
    }

    public static QueryParam build() {
        return new QueryParam();
    }

    public Map<String, Object> getParam() {
        return param;
    }

    public void setParam(Map<String, Object> param) {
        this.param = param;
    }
}
