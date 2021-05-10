package org.hswebframework.web.api.crud.entity;

import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.ezorm.core.param.TermType;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class TermExpressionParserTest {

    @Test
    public void test() {
        {
            List<Term> terms = TermExpressionParser.parse("name = 1");

            assertEquals(terms.get(0).getTermType(), TermType.eq);

        }

//        {
//            List<Term> terms = TermExpressionParser.parse("name = 1");
//
//            assertEquals(terms.get(0).getTermType(), TermType.not);
//
//        }
        {
            List<Term> terms = TermExpressionParser.parse("name > 1");

            assertEquals(terms.get(0).getTermType(), TermType.gt);
        }

        {
            List<Term> terms = TermExpressionParser.parse("name >= 1");

            assertEquals(terms.get(0).getTermType(), TermType.gte);
        }

        {
            List<Term> terms = TermExpressionParser.parse("name gte 1 and name not 1");

            assertEquals(terms.get(0).getTermType(), TermType.gte);
            assertEquals(terms.get(1).getTermType(), TermType.not);
        }

        {
            List<Term> terms = TermExpressionParser.parse("name gte 1 and (name not 1 or age gt 0)");

            assertEquals(terms.get(0).getTermType(), TermType.gte);
            assertEquals(terms.get(1).getTerms().get(0).getTermType(), TermType.not);
            assertEquals(terms.get(1).getTerms().get(1).getTermType(), TermType.gt);
        }
    }

}