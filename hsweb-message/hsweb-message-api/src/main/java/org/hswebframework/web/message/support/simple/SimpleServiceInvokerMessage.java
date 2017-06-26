package org.hswebframework.web.message.support.simple;

import org.hswebframework.web.message.support.ServiceInvokerMessage;

import java.io.Serializable;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimpleServiceInvokerMessage implements ServiceInvokerMessage {
    private String serviceName;

    private String method;

    private Serializable[] args;

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public Serializable[] getArgs() {
        return args;
    }

    public void setArgs(Serializable[] args) {
        this.args = args;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public SimpleServiceInvokerMessage(String serviceName, String method, Serializable... args) {
        this.serviceName = serviceName;
        this.method = method;
        this.args = args;
    }

    public SimpleServiceInvokerMessage() {
    }
}
