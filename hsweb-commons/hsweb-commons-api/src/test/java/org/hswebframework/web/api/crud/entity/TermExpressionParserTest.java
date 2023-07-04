package org.hswebframework.web.api.crud.entity;

import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.ezorm.core.param.TermType;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class TermExpressionParserTest {

    @Test
    public void testUrl(){
        List<Term> terms = TermExpressionParser.parse("type=email%20and%20provider=test");

        assertEquals(terms.get(0).getTermType(), TermType.eq);
        assertEquals(terms.get(0).getColumn(), "type");
        assertEquals(terms.get(0).getValue(), "email");

        assertEquals(terms.get(1).getTermType(), TermType.eq);
        assertEquals(terms.get(1).getColumn(), "provider");
        assertEquals(terms.get(1).getValue(), "test");

    }

    @Test
    public void testChinese() {
        {
            List<Term> terms = TermExpressionParser.parse("name = 我");

            assertEquals(terms.get(0).getTermType(), TermType.eq);
            assertEquals(terms.get(0).getValue(),"我");

        }

        {
            List<Term> terms = TermExpressionParser.parse("name like %我%");

            assertEquals(terms.get(0).getTermType(), TermType.like);
            assertEquals(terms.get(0).getValue(),"%我%");

        }
    }
    @Test
    public void testMap(){
        Map<String,Object> map = new LinkedHashMap<>();
        map.put("name$like","我");

        map.put("$or$name","你");

        map.put("$nest","age = 10");


        List<Term> terms = TermExpressionParser.parse(map);

        assertEquals(3,terms.size());
        assertEquals("like",terms.get(0).getTermType());
        assertEquals("name",terms.get(0).getColumn());
        assertEquals("我",terms.get(0).getValue());

        assertEquals(Term.Type.or,terms.get(1).getType());
        assertEquals("name",terms.get(1).getColumn());
        assertEquals("你",terms.get(1).getValue());

        assertEquals(1,terms.get(2).getTerms().size());

        assertEquals("age",terms.get(2).getTerms().get(0).getColumn());

    }


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