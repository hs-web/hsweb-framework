package org.hswebframework.web.logging;

import lombok.Getter;
import lombok.Setter;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 访问日志信息,此对象包含了被拦截的方法和类信息,如果要对此对象进行序列化,请自行转换为想要的格式.
 * 或者调用{@link this#toSimpleMap}获取可序列化的map格式日志信息
 *
 * @author zhouhao
 * @since 3.0
 */
@Getter
@Setter
public class AccessLoggerInfo {

    /**
     * 日志id
     */
    private String id;

    /**
     * 访问的操作
     *
     * @see AccessLogger#value()
     */
    private String action;

    /**
     * 描述
     *
     * @see AccessLogger#describe()
     */
    private String describe;

    /**
     * 访问对应的java方法
     */
    private Method method;

    /**
     * 访问对应的java类
     */
    private Class<?> target;

    /**
     * 请求的参数,参数为java方法的参数而不是http参数,key为参数名,value为参数值.
     */
    private Map<String, Object> parameters;

    /**
     * 请求者ip地址
     */
    private String ip;

    /**
     * 请求的url地址
     */
    private String url;

    /**
     * http 请求头集合
     */
    private Map<String, String> httpHeaders;

    /**
     * 上下文
     */
    private Map<String, String> context;

    /**
     * http 请求方法, GET,POST...
     */
    private String httpMethod;

    /**
     * 响应结果,方法的返回值
     */
    private Object response;

    /**
     * 请求时间戳
     *
     * @see System#currentTimeMillis()
     */
    private long requestTime;

    /**
     * 响应时间戳
     *
     * @see System#currentTimeMillis()
     */
    private long responseTime;

    /**
     * 异常信息,请求对应方法抛出的异常
     */
    private Throwable exception;

    public Map<String, Object> toSimpleMap(Function<Object, Serializable> objectFilter, Map<String, Object> map) {
        map.put("action", action);
        map.put("describe", describe);
        if (method != null) {
            StringJoiner methodAppender = new StringJoiner(",", method.getName().concat("("), ")");
            String[] parameterNames = parameters.keySet().toArray(new String[0]);
            Class<?>[] parameterTypes = method.getParameterTypes();

            for (int i = 0; i < parameterTypes.length; i++) {
                methodAppender.add(parameterTypes[i]
                                           .getSimpleName()
                                           .concat(" ")
                                           .concat(parameterNames.length > i ? parameterNames[i] : ("arg" + i)));
            }
            map.put("method", methodAppender.toString());
        }
        map.put("target", target != null ? target.getName() : "");
        Map<String, Object> newParameter = new LinkedHashMap<>(parameters);
        newParameter.entrySet().forEach(entry -> {
            if (entry.getValue() != null) {
                entry.setValue(objectFilter.apply(entry.getValue()));
            }
        });

        map.put("parameters", newParameter);
        map.put("httpHeaders", httpHeaders);
        map.put("httpMethod", httpMethod);
        map.put("ip", ip);
        map.put("url", url);
        map.put("response", objectFilter.apply(response));
        map.put("requestTime", requestTime);
        map.put("responseTime", responseTime);
        map.put("id", id);
        map.put("useTime", responseTime - requestTime);
        if (exception != null) {
            StringWriter writer = new StringWriter();
            exception.printStackTrace(new PrintWriter(writer));
            map.put("exception", writer.toString());
        }
        return map;
    }


    public void putAccessInfo(RequestInfo info) {
        setIp(info.getIpAddr());
        setHttpMethod(info.getRequestMethod());
        setHttpHeaders(info.getHeaders());
        setUrl(info.getPath());
        setContext(info.getContext());
    }

    public void putContext(Map<String,String> context) {
        if (this.context == null) {
            this.context = new ConcurrentHashMap<>();
        }
        this.context.putAll(context);
    }

    public void putContext(String key, Object value) {
        if (this.context == null) {
            this.context = new ConcurrentHashMap<>();
        }
        this.context.put(key, String.valueOf(value));
    }
}
