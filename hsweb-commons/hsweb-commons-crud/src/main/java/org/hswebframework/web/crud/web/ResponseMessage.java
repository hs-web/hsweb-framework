package org.hswebframework.web.crud.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseMessage<T> implements Serializable {

    private static final long serialVersionUID = 8992436576262574064L;

    @Schema(description = "消息提示")
    protected String message;

    @Schema(description = "数据内容")
    protected T result;

    @Schema(description = "状态码")
    private int status;

    @Schema(description = "业务码")
    protected String code;

    @Schema(description = "时间戳(毫秒)")
    protected Long timestamp = System.currentTimeMillis();

    public static <T> ResponseMessage<T> ok() {
        return ok(null);
    }

    @SuppressWarnings("all")
    public static <T> ResponseMessage<T> ok(T result) {
        return (ResponseMessage) ResponseMessage.builder()
                .result(result)
                .status(200)
                .code("success")
                .build();
    }

    public static <T> ResponseMessage<T> error(String message) {
        return error("error", message);
    }

    public static <T> ResponseMessage<T> error(String code, String message) {
        return error(500, code, message);
    }

    public static <T> ResponseMessage<T> error(int status, String code, String message) {
        ResponseMessage<T> msg = new ResponseMessage<>();
        msg.message = message;
        msg.code = code;
        msg.status = status;
        return msg;
    }

    public ResponseMessage<T> result(T result) {
        this.result = result;
        return this;
    }
}
