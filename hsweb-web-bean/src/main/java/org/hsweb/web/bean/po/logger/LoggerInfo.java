package org.hsweb.web.bean.po.logger;

import org.hsweb.web.bean.po.GenericPo;

/**
 * Created by 浩 on 2015-09-11 0011.
 */
public class LoggerInfo extends GenericPo<String> {
    private static final long serialVersionUID = 8910856253780046561L;
    /**
     * 请求者ip
     */
    private String client_ip;

    /**
     * 请求路径
     */
    private String request_uri;

    /**
     * 完整路径
     */
    private String request_url;

    /**
     * 对应的方法,格式为 HTTP方法+java方法 如:GET.list()
     */
    private String request_method;

    /**
     * 响应结果
     */
    private String response_content;

    /**
     * 用户主键
     */
    private String user_id;

    /**
     * 请求时间
     */
    private long request_time;

    /**
     * 响应时间
     */
    private long response_time;

    /**
     * 请求耗时
     */
    private long use_time = -1;

    /**
     * referer信息
     */
    private String referer;

    /**
     * 客户端标识
     */
    private String user_agent;

    /**
     * 响应码
     */
    private String response_code;

    /**
     * 请求头信息
     */
    private String request_header;

    /**
     * 对应类名
     */
    private String class_name;

    /**
     * 功能摘要
     */
    private String module_desc;

    /**
     * 请求参数
     */
    private String request_param;

    /**
     * 响应异常
     */
    private String exception_info;

    /**
     * 命中缓存
     */
    private String cache_key;


    public String getUser_id() {
        if (user_id == null)
            user_id = "-";
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public long getRequest_time() {
        return request_time;
    }

    public void setRequest_time(long request_time) {
        this.request_time = request_time;
    }

    public long getResponse_time() {
        return response_time;
    }

    public void setResponse_time(long response_time) {
        this.response_time = response_time;
    }

    public long getUse_time() {
        if (use_time == -1)
            use_time = getResponse_time() - getRequest_time();
        return use_time;
    }

    public void setUse_time(long use_time) {
        this.use_time = use_time;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public String getUser_agent() {
        return user_agent;
    }

    public void setUser_agent(String user_agent) {
        this.user_agent = user_agent;
    }


    public String getClass_name() {
        return class_name;
    }

    public void setClass_name(String class_name) {
        this.class_name = class_name;
    }

    public String getClient_ip() {
        return client_ip;
    }

    public void setClient_ip(String client_ip) {
        this.client_ip = client_ip;
    }

    public String getRequest_uri() {
        return request_uri;
    }

    public void setRequest_uri(String request_uri) {
        this.request_uri = request_uri;
    }

    public String getRequest_url() {
        return request_url;
    }

    public void setRequest_url(String request_url) {
        this.request_url = request_url;
    }

    public String getRequest_method() {
        return request_method;
    }

    public void setRequest_method(String request_method) {
        this.request_method = request_method;
    }

    public String getResponse_content() {
        return response_content;
    }

    public void setResponse_content(String response_content) {
        this.response_content = response_content;
    }

    public String getResponse_code() {
        return response_code;
    }

    public void setResponse_code(String response_code) {
        this.response_code = response_code;
    }

    public String getRequest_header() {
        return request_header;
    }

    public void setRequest_header(String request_header) {
        this.request_header = request_header;
    }

    public String getModule_desc() {
        return module_desc;
    }

    public void setModule_desc(String module_desc) {
        this.module_desc = module_desc;
    }

    public String getRequest_param() {
        return request_param;
    }

    public void setRequest_param(String request_param) {
        this.request_param = request_param;
    }

    public String getException_info() {
        return exception_info;
    }

    public void setException_info(String exception_info) {
        this.exception_info = exception_info;
    }

    public String getCache_key() {
        return cache_key;
    }

    public void setCache_key(String cache_key) {
        this.cache_key = cache_key;
    }
}
