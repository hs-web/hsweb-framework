package org.hswebframework.web.commons.beans;

import org.hswebframework.web.commons.beans.dsl.QueryBean;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class QueryBeanTests {

    public static void main(String[] args) {


    }
}

class TestQuery implements QueryBean<TestQuery> {

    public final TestQuery name = new TestQuery();
    public final TestQuery age = new TestQuery();


    @Override
    public TestQuery like(Object value) {
        return null;
    }

    @Override
    public TestQuery gt(Object value) {
        return null;
    }
}
