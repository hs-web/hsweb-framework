package org.hsweb.web.mybatis.plgins.pager.dialect;

import org.springframework.stereotype.Component;

/**
 * Created by zhouhao on 16-4-13.
 */
@Component
public class H2PagerHelper extends MysqlPagerHelper {

    @Override
    public String getDialect() {
        return "h2";
    }
}
