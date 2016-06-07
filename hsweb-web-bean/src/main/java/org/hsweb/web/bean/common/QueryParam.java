package org.hsweb.web.bean.common;


import org.hsweb.ezorm.param.Term;
import org.hsweb.ezorm.param.TermType;

import java.io.Serializable;
import java.util.List;

/**
 * Created by 浩 on 2016-01-16 0016.
 */
public class QueryParam extends org.hsweb.ezorm.param.QueryParam<QueryParam> implements Serializable {
    private static final long serialVersionUID = 7941767360194797891L;

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
}
