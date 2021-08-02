package org.hswebframework.web.utils;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;
import java.util.Optional;


public class ReactiveWebUtils {

    static final String[] ipHeaders = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP"
    };

    /**
     * 获取请求客户端的真实ip地址
     *
     * @param request 请求对象
     * @return ip地址
     */
    public static String getIpAddr(ServerHttpRequest request) {
        for (String ipHeader : ipHeaders) {
            String ip = request.getHeaders().getFirst(ipHeader);
            if (!StringUtils.isEmpty(ip) && !ip.contains("unknown")) {
                return ip;
            }
        }
        return Optional.ofNullable(request.getRemoteAddress())
                .map(addr->addr.getAddress().getHostAddress())
                .orElse("unknown");
    }

}
