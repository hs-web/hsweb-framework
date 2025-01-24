package org.hswebframework.web.exception;

import org.junit.Test;

import static org.junit.Assert.*;

public class TraceSourceExceptionTest {


    @Test
    public void test() {

        TraceSourceException exp = new TraceSourceException()
            .withSource("test","testSource");


        {
            assertEquals("test", TraceSourceException.tryGetOperation(exp));

            assertEquals("testSource", TraceSourceException.tryGetSource(exp));

        }
        {
            RuntimeException e = new RuntimeException();
            e.addSuppressed(exp);
            assertEquals("test", TraceSourceException.tryGetOperation(e));

            assertEquals("testSource", TraceSourceException.tryGetSource(e));
            e.printStackTrace();
        }

        {
            assertEquals("test", TraceSourceException
                .tryGetOperation(
                    new RuntimeException(exp)
                ));
            assertEquals("testSource", TraceSourceException
                .tryGetSource(
                    new RuntimeException(exp)
                ));

        }

    }
}