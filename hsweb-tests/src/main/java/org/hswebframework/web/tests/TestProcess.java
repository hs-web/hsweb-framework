package org.hswebframework.web.tests;

public interface TestProcess {
    TestProcess setUp(TestProcessSetUp testProcessSetUp);

    TestResult exec() throws Exception;

}
