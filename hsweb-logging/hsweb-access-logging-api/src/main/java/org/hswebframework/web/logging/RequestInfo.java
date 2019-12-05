package org.hswebframework.web.logging;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestInfo {

    private String requestId;

    private String ipAddr;

    private String path;

    private String requestMethod;

    private String userId;

    private String username;

    private Map<String,String> headers;

    private Map<String,String> context;


}
