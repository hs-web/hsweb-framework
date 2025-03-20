package org.hswebframework.web.starter.reporter;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.exception.analyzer.ExceptionAnalyzers;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringBootExceptionReporter;
import org.springframework.context.ConfigurableApplicationContext;

@Slf4j
public class GenericExceptionReport implements SpringBootExceptionReporter {


    public GenericExceptionReport(ConfigurableApplicationContext context) {
    }


    @Override
    public boolean reportException(Throwable failure) {
        return ExceptionAnalyzers.analyze(failure);
    }

}
