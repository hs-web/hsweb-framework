package org.hswebframework.web.controller.message;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author zhouhao
 */
public class MapResponseMessage extends ResponseMessage<Map<String, Object>> {
    public MapResponseMessage() {
        result(new LinkedHashMap<>());
    }

    public MapResponseMessage put(String key, Object value) {
        result.put(key, value);
        return this;
    }

    public static MapResponseMessage ok() {
        return new MapResponseMessage();
    }

    public static MapResponseMessage ok(String message) {
        MapResponseMessage responseMessage = new MapResponseMessage();
        responseMessage.message = message;
        return responseMessage;
    }

    public static MapResponseMessage error() {
        return new MapResponseMessage();
    }


    public static MapResponseMessage error(String message) {
        MapResponseMessage mapResponseMessage = new MapResponseMessage();
        mapResponseMessage.message = message;
        return mapResponseMessage;
    }

    public static MapResponseMessage error(int status, String message) {
        MapResponseMessage mapResponseMessage = new MapResponseMessage();
        mapResponseMessage.message = message;
        mapResponseMessage.status = status;
        return mapResponseMessage;
    }
}
