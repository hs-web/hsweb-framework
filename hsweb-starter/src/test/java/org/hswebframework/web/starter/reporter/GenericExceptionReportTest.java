package org.hswebframework.web.starter.reporter;

import org.hswebframework.web.exception.analyzer.ExceptionAnalyzers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;


public class GenericExceptionReportTest {


    @Test
    void test(){
        GenericExceptionReport report = new GenericExceptionReport(
            new GenericApplicationContext()
        );

        Assertions.assertTrue(
            report.reportException(new IndexOutOfBoundsException("Binding index 0 when only 0 parameters are expected "))
        );

    }

}