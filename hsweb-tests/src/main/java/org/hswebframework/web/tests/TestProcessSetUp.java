package org.hswebframework.web.tests;

import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

public interface TestProcessSetUp {
    void setUp(MockHttpServletRequestBuilder requestBuilder);
}