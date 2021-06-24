package org.hswebframework.web.crud.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.api.crud.entity.EntityFactoryHolder;

import java.io.Serializable;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseMessage<T> implements Serializable {

    private static final long serialVersionUID = 8992436576262574064L;

    @Schema(description = "消息提示")
    private String message;

    @Schema(description = "数据内容")
    private T result;

    @Schema(description = "状态码")
    private int status;

    @Schema(description = "业务码")
    private String code;

    @Schema(description = "时间戳(毫秒)")
    private Long timestamp = System.currentTimeMillis();

    public ResponseMessage() {
    }

    public static <T> ResponseMessage<T> ok() {
        return ok(null);
    }

    @SuppressWarnings("all")
    public static <T> ResponseMessage<T> ok(T result) {
        return of("success", result, 200, null, System.currentTimeMillis());
    }

    public static <T> ResponseMessage<T> error(String message) {
        return error("error", message);
    }

    public static <T> ResponseMessage<T> error(String code, String message) {
        return error(500, code, message);
    }

    public static <T> ResponseMessage<T> error(int status, String code, String message) {
        return of(message, null, status, code, System.currentTimeMillis());
    }

    public static <T> ResponseMessage<T> of(String message,
                                            T result,
                                            int status,
                                            String code,
                                            Long timestamp) {
        @SuppressWarnings("all")
        ResponseMessage<T> msg = EntityFactoryHolder.newInstance(ResponseMessage.class, ResponseMessage::new);
        msg.setMessage(message);
        msg.setResult(result);
        msg.setStatus(status);
        msg.setCode(code);
        msg.setTimestamp(timestamp);
        return msg;
    }

    public ResponseMessage<T> result(T result) {
        this.result = result;
        return this;
    }
}
