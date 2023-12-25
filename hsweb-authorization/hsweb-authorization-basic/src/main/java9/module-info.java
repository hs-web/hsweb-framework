module hsweb.authorization.basic {
    requires spring.core;
    requires hsweb.core;
    requires hsweb.authorization.api;
    requires hsweb.access.logging.api;
    requires spring.beans;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.boot;
    requires reactor.core;
    requires static lombok;
    requires fastjson;
    requires commons.collections;
    requires com.fasterxml.jackson.annotation;
    requires jakarta.annotation;
    requires org.slf4j;
    requires spring.aop;
    requires org.reactivestreams;
    requires spring.web;
    requires org.apache.commons.collections4;
    requires jakarta.validation;
    requires io.swagger.v3.oas.annotations;
    requires spring.webmvc;
    requires jakarta.servlet;
    requires org.apache.commons.codec;

    exports org.hswebframework.web.authorization.basic.web;
    exports org.hswebframework.web.authorization.basic.aop;
    exports org.hswebframework.web.authorization.basic.configuration;
    exports org.hswebframework.web.authorization.basic.define;

    opens org.hswebframework.web.authorization.basic.aop;
    opens org.hswebframework.web.authorization.basic.configuration;
}