package org.hswebframework.web.crud.exception;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.crud.configuration.DialectProvider;
import org.hswebframework.web.crud.configuration.DialectProviders;
import org.hswebframework.web.exception.analyzer.ExceptionAnalyzerReporter;

import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class DatabaseExceptionAnalyzerReporter extends ExceptionAnalyzerReporter {

    public DatabaseExceptionAnalyzerReporter() {
        init();
    }

    void init() {
        addSimpleReporter(
            Pattern.compile("^Binding.*"),
            error -> log
                .warn(wrapLog("请在application.yml中正确配置`easyorm.dialect`,可选项为:{}"),
                      DialectProviders
                          .all()
                          .stream()
                          .map(DialectProvider::name)
                          .collect(Collectors.toList())
                    , error));

        addSimpleReporter(
            Pattern.compile("^Unknown database.*"),
            error -> log
                .warn(wrapLog("请先手动创建数据库或者配置`easyorm.default-schema`,数据库名不能包含只能由`数字字母下划线`组成."), error));

        initForPgsql();
    }

    void initForPgsql() {
        addSimpleReporter(
            Pattern.compile(".*\\[3D000].*"),
            error -> log
                .warn(wrapLog("请先手动创建数据库,数据库名不能包含只能由`数字字母下划线`组成."), error));

        addSimpleReporter(
            Pattern.compile(".*\\[3F000].*"),
            error -> log
                .warn(wrapLog("请正确配置`easyorm.default-schema`为pgsql数据库中对应的schema."), error));

        addReporter(
            err->err.getClass().getCanonicalName().contains("PostgresConnectionException"),
            error -> log
                .warn(wrapLog("请检查数据库连接配置是否正确."), error));
    }
}
