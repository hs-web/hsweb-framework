package org.hswebframework.web.crud.query;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QueryHelperUtilsTest {


    @Test
    void testToHump(){

        assertEquals("testName",QueryHelperUtils.toHump("test_name"));


        assertEquals("ruownum_",QueryHelperUtils.toHump("RUOWNUM_"));

    }

    @Test
    void testToSnake(){

        assertEquals("test_name",QueryHelperUtils.toSnake("testName"));

        assertEquals("test_name",QueryHelperUtils.toSnake("TestName"));



    }
}