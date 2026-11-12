package org.hswebframework.web.datasource.switcher;

import java.util.Optional;

/**
 * 同步数据源或 Schema 切换接口。
 *
 * @deprecated 动态切换能力已弃用，仅为已有调用保留。
 */
@Deprecated
public interface Switcher {

    void useLast();

    void use(String id);

    void useDefault();

    Optional<String> current();

    void reset();

}
