package org.hswebframework.web.authorization.token;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.dict.EnumDict;

/**
 * 令牌状态
 */
@Getter
@AllArgsConstructor
public enum TokenState implements EnumDict<String> {
    /**
     * 正常，有效
     */
    normal("normal","message.token_state_normal"),

    /**
     * 已被禁止访问
     */
    deny("deny", "message.token_state_deny"),

    /**
     * 已过期
     */
    expired("expired", "message.token_state_expired"),

    /**
     * 已被踢下线
     * @see AllopatricLoginMode#offlineOther
     */
    offline("offline", "message.token_state_offline"),

    /**
     * 锁定
     */
    lock("lock", "message.token_state_lock");

    private final String value;

    private final String text;
}
