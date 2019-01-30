package org.hswebframework.web.authorization.access;


/**
 * 和user关联的实体
 *
 * @author zhouhao
 * @since 3.0.6
 */
public interface UserAttachEntity {
    String userId = "userId";

    String getUserId();

    void setUserId(String userId);

    default String getUserIdProperty() {
        return userId;
    }
}
