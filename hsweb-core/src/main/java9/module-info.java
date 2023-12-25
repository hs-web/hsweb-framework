module hsweb.core{
    requires org.hibernate.validator;
    requires org.reactivestreams;
    requires hsweb.utils;
    requires com.google.common;
    requires reactor.core;
    requires fastjson;
    requires spring.beans;
    requires spring.core;
    requires java.desktop;
    requires commons.beanutils;
    requires jctools.core;
    requires org.aspectj.weaver;
    requires reactor.extra;
    requires io.netty.common;
    requires io.seruco.encoding.base62;
    requires spring.web;
    requires jakarta.servlet;
    requires jakarta.validation;
    requires spring.context;
    requires io.swagger.v3.oas.annotations;
    requires org.slf4j;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires spring.aop;
    requires static lombok;
    requires jsr305;
    requires jakarta.annotation;
    requires org.javassist;
    requires java.base;
    requires org.apache.commons.codec;


    exports org.hswebframework.web;
    exports org.hswebframework.web.aop;
    exports org.hswebframework.web.bean;
    exports org.hswebframework.web.context;
    exports org.hswebframework.web.convert;
    exports org.hswebframework.web.dict;
    exports org.hswebframework.web.dict.defaults;
    exports org.hswebframework.web.enums;
    exports org.hswebframework.web.event;
    exports org.hswebframework.web.exception;
    exports org.hswebframework.web.i18n;
    exports org.hswebframework.web.id;
    exports org.hswebframework.web.logger;
    exports org.hswebframework.web.proxy;
    exports org.hswebframework.web.utils;
    exports org.hswebframework.web.validator;

    opens org.hswebframework.web.validator;

}