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

    private Map<String,String> headers;


}
