package org.hsweb.web.core.utils;

import org.hsweb.web.bean.po.user.User;
import org.hsweb.web.core.authorize.oauth2.OAuth2Manager;
import org.hsweb.web.core.authorize.oauth2.OAuth2ManagerHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Web常用工具集，用于获取当前登录用户，请求信息等
 * Created by 浩 on 2015-08-25 0025.
 */
public class WebUtil {

    /**
     * 尝试获取当前请求的HttpServletRequest实例
     *
     * @return HttpServletRequest
     */
    public static HttpServletRequest getHttpServletRequest() {
        try {
            return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 尝试获取当前登录的用户（基于ThreadLocal）
     *
     * @return 当前登录的用户
     */
    public static User getLoginUser() {
        return getLoginUser(getHttpServletRequest());
    }

    /**
     * 在HttpSession中获取当前登录的用户
     *
     * @param session HttpSession
     * @return 当前登录的用户
     */
    public static User getLoginUser(HttpSession session) {
        try {
            return (User) session.getAttribute("user");
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 在HttpServletRequest中获取当前登录的用户
     *
     * @param request HttpServletRequest
     * @return 当前登录的用户
     */
    public static User getLoginUser(HttpServletRequest request) {
        if (request == null) return null;
        HttpSession session = request.getSession(false);
        if (session == null) {
            OAuth2Manager manager = OAuth2ManagerHolder.getManager();
            if (manager != null) {
                return manager.getUserByRequest(request);
            }
            return null;
        }
        return getLoginUser(session);
    }

    public static Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> map = new LinkedHashMap<>();
        Enumeration<String> enumeration = request.getHeaderNames();
        while (enumeration.hasMoreElements()) {
            String key = enumeration.nextElement();
            String value = request.getHeader(key);
            map.put(key, value);
        }
        return map;
    }

    /**
     * 解析参数列表为map
     *
     * @param request request请求对象
     * @return 参数集合
     */
    public static Map<String, String> getParams(HttpServletRequest request) {
        Map<String, String[]> map = request.getParameterMap();
        Map<String, String> param = new LinkedHashMap<>();
        for (Map.Entry<String, String[]> entry : map.entrySet()) {
            String key = entry.getKey();
            String[] varr = entry.getValue();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < varr.length; i++) {
                String var = varr[i];
                if (i != 0) builder.append(",");
                builder.append(var);
            }
            param.put(key, builder.toString());
        }
        return param;
    }

    /**
     * 获取请求客户端的真实ip地址
     *
     * @param request 请求对象
     * @return ip地址
     */
    public static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader(" x-forwarded-for ");
        if (ip == null || ip.length() == 0 || " unknown ".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || " unknown ".equalsIgnoreCase(ip)) {
            ip = request.getHeader(" Proxy-Client-IP ");
        }
        if (ip == null || ip.length() == 0 || " unknown ".equalsIgnoreCase(ip)) {
            ip = request.getHeader(" WL-Proxy-Client-IP ");
        }
        if (ip == null || ip.length() == 0 || " unknown ".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * web应用绝对路径
     *
     * @param request 请求对象
     * @return 绝对路径
     */
    public static String getBasePath(HttpServletRequest request) {
        String path = request.getContextPath();
        String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/";
        return basePath;
    }

}
