package org.hswebframework.web.crud.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;

public class DatabaseExceptionAnalyzerReporterTest {

    DatabaseExceptionAnalyzerReporter reporter=new DatabaseExceptionAnalyzerReporter();
    @Test
    void testBinding(){
        Assertions.assertTrue(reporter.doReportException(
            new IndexOutOfBoundsException("Binding index 0 when only 0 parameters are expected ")
        ));

        Assertions.assertTrue(reporter.doReportException(
            new IndexOutOfBoundsException("Binding parameters is not supported for simple statement")
        ));
    }

    @Test
    void testUnknownDatabase(){
        Assertions.assertTrue(reporter.doReportException(
            new IndexOutOfBoundsException("Unknown database 'jetlinks' ")
        ));
    }


    @Test
    void testPgsqlUnknownDatabase(){
        Assertions.assertTrue(reporter.doReportException(
            new IndexOutOfBoundsException("[3D000] database \"jetlinks22\" does not exist")
        ));
    }
    @Test
    void testPgsqlUnknownSchema(){
        Assertions.assertTrue(reporter.doReportException(
            new IndexOutOfBoundsException("[3F000] schema \"jetlinks22\" does not exist")
        ));
    }
}