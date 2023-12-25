module hsweb.authorization.api {
    requires spring.core;
    requires hsweb.core;
    requires spring.beans;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.boot;
    requires static spring.data.redis;
    requires reactor.core;
    requires static lombok;
    requires fastjson;
    requires commons.collections;
    requires com.fasterxml.jackson.annotation;
    requires jakarta.annotation;
    requires org.slf4j;


    exports org.hswebframework.web.authorization;
    exports org.hswebframework.web.authorization.access;
    exports org.hswebframework.web.authorization.annotation;
    exports org.hswebframework.web.authorization.token.redis;
    exports org.hswebframework.web.authorization.token.event;
    exports org.hswebframework.web.authorization.builder;
    exports org.hswebframework.web.authorization.define;
    exports org.hswebframework.web.authorization.dimension;
    exports org.hswebframework.web.authorization.events;
    exports org.hswebframework.web.authorization.exception;
    exports org.hswebframework.web.authorization.setting;
    exports org.hswebframework.web.authorization.simple;
    exports org.hswebframework.web.authorization.simple.builder;
    exports org.hswebframework.web.authorization.twofactor.defaults;
    exports org.hswebframework.web.authorization.twofactor;

    opens org.hswebframework.web.authorization.simple;
    exports org.hswebframework.web.authorization.token;

}